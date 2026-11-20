package dev.ariel.tfm.products.util.fault;

import dev.ariel.tfm.products.util.enums.FaultMode;
import dev.ariel.tfm.products.util.properties.FaultProps;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Inyecta fallos controlados (latencia, errores 5xx o esperas largas) segun la configuracion fault.*.
 * Permite construir versiones defectuosas para los experimentos sin cambiar el codigo de negocio.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FaultInjector {

    private final FaultProps props;

    void onStart(@Observes StartupEvent event) {
        log.info("Inyeccion de fallos configurada, mode: {}, latencyMs: {}, timeoutMs: {}, probability: {}",
            props.mode(), props.latencyMs(), props.timeoutMs(), props.probability());
    }

    /**
     * Aplica el fallo configurado a la peticion en curso. Si el modo es NONE no hace nada.
     */
    public void apply() {
        if (props.mode() == FaultMode.NONE || !isSelected())
            return;

        switch (props.mode()) {
            case LATENCY -> sleep(props.latencyMs());
            case TIMEOUT -> sleep(props.timeoutMs());
            case ERROR -> {
                log.warn("Fallo inyectado: error HTTP 500");
                throw new InternalServerErrorException("Fallo inyectado para experimentacion");
            }
            default -> { }
        }
    }

    private boolean isSelected() {
        return ThreadLocalRandom.current().nextDouble() < props.probability();
    }

    private void sleep(long millis) {
        log.warn("Fallo inyectado: espera de {} ms, mode: {}", millis, props.mode());
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
