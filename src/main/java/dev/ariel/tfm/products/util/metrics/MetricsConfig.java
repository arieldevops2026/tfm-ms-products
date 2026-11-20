package dev.ariel.tfm.products.util.metrics;

import dev.ariel.tfm.products.util.properties.AppProps;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.stream.LongStream;

/**
 * Configuracion de metricas: etiquetas comunes por version y buckets de latencia alineados con el SLO.
 */
@ApplicationScoped
public class MetricsConfig {

    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    private static final String HTTP_CLIENT_REQUESTS = "http.client.requests";

    /**
     * Buckets del histograma en milisegundos. Incluye 500 ms para calcular con exactitud el SLO de latencia.
     */
    private static final long[] LATENCY_BUCKETS_MS = {25, 50, 100, 200, 300, 400, 500, 750, 1000, 2000, 5000};

    /**
     * Etiquetas que identifican servicio, version y entorno en todas las metricas
     */
    @Produces
    @Singleton
    public MeterFilter commonTags(AppProps app, @ConfigProperty(name = "quarkus.application.name") String application) {
        return MeterFilter.commonTags(Tags.of(
            "service_name", application,
            "service_version", app.version(),
            "deployment_environment", app.environment()));
    }

    /**
     * Histograma de latencia HTTP con buckets fijos, necesario para histogram_quantile y para el SLI de latencia
     */
    @Produces
    @Singleton
    public MeterFilter httpLatencyBuckets() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                String name = id.getName();
                if (!HTTP_SERVER_REQUESTS.equals(name) && !HTTP_CLIENT_REQUESTS.equals(name))
                    return config;

                double[] bucketsNanos = LongStream.of(LATENCY_BUCKETS_MS)
                    .mapToDouble(ms -> Duration.ofMillis(ms).toNanos())
                    .toArray();

                return DistributionStatisticConfig.builder()
                    .serviceLevelObjectives(bucketsNanos)
                    .build()
                    .merge(config);
            }
        };
    }

}
