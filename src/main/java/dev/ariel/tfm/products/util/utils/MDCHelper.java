package dev.ariel.tfm.products.util.utils;

import dev.ariel.tfm.products.util.constants.Header;

import java.util.Optional;
import org.slf4j.MDC;

public class MDCHelper {

    private MDCHelper() {}

    public static void setUuid(String uuid) {
        MDC.put(Header.UUID, uuid);
    }

    public static String getUuid() {
        return Optional.ofNullable(MDC.get(Header.UUID))
            .orElse("");
    }

    public static void setIp(String ip) {
        MDC.put(Header.IP, ip);
    }

    public static String getIp() {
        return Optional.ofNullable(MDC.get(Header.IP))
            .orElse("");
    }

    public static void setChannel(String channel) {
        MDC.put(Header.CHANNEL, channel);
    }

    public static String getChannel() {
        return Optional.ofNullable(MDC.get(Header.CHANNEL))
            .orElse("");
    }

    public static void setUser(String user) {
        MDC.put(Header.USER, user);
    }

    public static String getUser() {
        return Optional.ofNullable(MDC.get(Header.USER))
            .orElse("");
    }

    public static void setAccount(String account) {
        MDC.put(Header.ACCOUNT, account);
    }

    public static String getAccount() {
        return Optional.ofNullable(MDC.get(Header.ACCOUNT))
            .orElse("");
    }
}
