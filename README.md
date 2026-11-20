# tfm-ms-products

Microservicio de **catálogo de productos** del TFM *"Diseño y evaluación de una plataforma GitOps con entrega progresiva gobernada por SLO y telemetría OpenTelemetry para microservicios en Kubernetes"* (Máster en DevOps, UNIR).

Forma parte de la aplicación experimental de tres servicios sobre la que se evalúa la estrategia de despliegue canary gobernada por objetivos de nivel de servicio (SLO). No es un servicio de producción: su lógica es deliberadamente simple para que las diferencias medidas se deban a la estrategia de despliegue y no al negocio.

## Papel en el laboratorio

```text
cliente ──► ms-orders ──► ms-products   (este servicio: valora cada item)
                     └──► ms-payments
```

`ms-orders` consulta aquí el nombre y el precio de cada producto de una orden. Una versión defectuosa de este servicio degrada también a `ms-orders`, lo que permite estudiar la propagación de fallos entre dependencias.

## Tecnología

- Quarkus 3.40, Java 25, hilos virtuales (`@RunOnVirtualThread`).
- Compilación nativa con GraalVM/Mandrel para la imagen de contenedor.
- Lombok, Hibernate Validator, SmallRye OpenAPI y Health.
- Micrometer (métricas Prometheus) y OpenTelemetry (trazas y logs).

## Datos

Catálogo **en memoria, de solo lectura**, con 12 productos (`PRD-001` a `PRD-012`) en las categorías `TECNOLOGIA`, `HOGAR`, `DEPORTES` y `LIBROS`. No usa base de datos: los datos son idénticos en todas las réplicas y en cada arranque, de modo que revertir una versión no requiere recuperar datos.

## API

Base: `http://localhost:18001/api/v1`. Contrato completo en `src/main/resources/META-INF/openapi.yaml` y en Swagger UI (`/swagger-ui`).

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/products/get-list` | Lista paginada, con filtro opcional por categoría |
| `GET` | `/products/get-by-id/{id}` | Detalle de un producto. `404` si no existe |

```bash
curl localhost:18001/api/v1/products/get-by-id/PRD-001

curl -X POST localhost:18001/api/v1/products/get-list \
  -H 'Content-Type: application/json' \
  -d '{"pageNumber":1,"maxRecords":5,"category":"TECNOLOGIA"}'
```

Cabeceras opcionales de trazabilidad: `session-id`, `username`, `client-ip`, `channel`. El `session-id` se registra como `uuid` en todos los logs de la petición.

## Configuración

Toda la configuración se sobrescribe con variables de entorno, sin cambiar el código. Así, una misma imagen sirve como versión estable o como versión candidata defectuosa.

| Variable | Por defecto | Uso |
|---|---|---|
| `APP_VERSION` | `1.0.0` | Versión que aparece en métricas, trazas y logs (`service_version`) |
| `APP_ENVIRONMENT` | `local` | Entorno (`deployment_environment`) |
| `QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | OpenTelemetry Collector |
| `QUARKUS_OTEL_SDK_DISABLED` | `false` | `true` para no enviar telemetría |
| `FAULT_MODE` | `NONE` | Fallo a inyectar (ver abajo) |
| `FAULT_LATENCY_MS` | `0` | Retardo del modo `LATENCY` |
| `FAULT_TIMEOUT_MS` | `10000` | Espera del modo `TIMEOUT` |
| `FAULT_PROBABILITY` | `1.0` | Proporción de peticiones afectadas (0.0 a 1.0) |

## Inyección de fallos

Permite construir las versiones defectuosas de los experimentos:

| `FAULT_MODE` | Efecto |
|---|---|
| `NONE` | Sin fallo (versión saludable) |
| `LATENCY` | Añade `FAULT_LATENCY_MS` a la respuesta |
| `ERROR` | Responde HTTP 500 |
| `TIMEOUT` | Espera `FAULT_TIMEOUT_MS`, por encima del timeout de `ms-orders` (3 s) |

```bash
# Versión candidata 1.1.0 con 700 ms de latencia en el 30 % de las peticiones
APP_VERSION=1.1.0 FAULT_MODE=LATENCY FAULT_LATENCY_MS=700 FAULT_PROBABILITY=0.3 \
  java -jar target/quarkus-app/quarkus-run.jar
```

## Observabilidad

| Señal | Dónde | Uso en el TFM |
|---|---|---|
| Métricas | `/q/metrics` (Prometheus las consulta) | **Decisión** del canary: SLI de latencia y errores |
| Trazas | OTLP al Collector, luego Tempo | Diagnóstico |
| Logs | OTLP al Collector, luego Loki | Diagnóstico, enlazados a la traza por `trace_id` |
| Salud | `/q/health`, `/q/health/ready`, `/q/health/live` | Sondas de Kubernetes |

Las métricas HTTP llevan las etiquetas `service_name`, `service_version` y `deployment_environment`, y el histograma de latencia incluye un bucket exacto en **500 ms**, el umbral del SLO. Ejemplo del SLI de latencia:

```promql
histogram_quantile(0.95, sum by (le, service_version) (
  rate(http_server_requests_seconds_bucket{service_name="ms-products"}[1m])))
```

## Ejecutar

```bash
./mvnw quarkus:dev                                    # desarrollo, con recarga en caliente
./mvnw package && java -jar target/quarkus-app/quarkus-run.jar   # JVM
```

En modo dev la telemetría se controla con `%dev.quarkus.otel.sdk.disabled` en `application.properties`. Para ver trazas y logs en local, levanta antes el stack de observabilidad del laboratorio (`local-stack`).

## Imagen nativa

```bash
./mvnw package -Pnative -DskipTests -Dquarkus.native.container-build=true   # ~3 min, no requiere GraalVM local
docker build -f src/main/docker/Dockerfile.native-micro \
  --build-arg GIT_SHA=$(git rev-parse HEAD) -t tfm-ms-products:local .
docker run --rm -p 18001:18001 -e QUARKUS_OTEL_SDK_DISABLED=true tfm-ms-products:local
```

La imagen parte de una base mínima fijada por digest, corre con un usuario sin privilegios (`1001`) e incluye en la etiqueta `org.opencontainers.image.revision` el commit del que se construyó. Medido en local: arranque en unos 20 ms y unos 11 MB de memoria recién iniciada, frente a 1,2 s y unos 214 MB en JVM.

## Limitaciones

- Datos en memoria: no hay persistencia ni escritura.
- Sin autenticación: está pensado para un laboratorio aislado.
- El rendimiento absoluto de la imagen nativa no es comparable con una ejecución en JVM.
