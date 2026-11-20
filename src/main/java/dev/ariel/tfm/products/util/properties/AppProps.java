package dev.ariel.tfm.products.util.properties;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppProps {

    String version();

    String environment();
}
