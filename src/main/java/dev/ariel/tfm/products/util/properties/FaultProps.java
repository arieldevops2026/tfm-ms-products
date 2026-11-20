package dev.ariel.tfm.products.util.properties;

import dev.ariel.tfm.products.util.enums.FaultMode;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "fault")
public interface FaultProps {

    FaultMode mode();

    long latencyMs();

    long timeoutMs();

    double probability();
}
