package dev.ariel.tfm.products.api.service;

import dev.ariel.tfm.products.util.utils.GeneralUtils;
import dev.ariel.tfm.products.util.constants.Header;
import dev.ariel.tfm.products.util.utils.MDCHelper;

import jakarta.enterprise.context.ApplicationScoped;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class RequestService {

    private static final String LOG_REQUEST_RECIBIDO = "Peticion recibida, Desde: {}, path: {}, headers: {}, body: {}";
    private static final String LOG_RESPONSE_ENVIADO = "Respuesta enviada: {}";

    public void onInitialProcess(HttpServerRequest context, Object request) {
        MDCHelper.setUuid(GeneralUtils.coalesce(context.getHeader(Header.SESSION_ID), context.getHeader(Header.UUID), UUID.randomUUID().toString()));
        MDCHelper.setIp(GeneralUtils.coalesce(context.getHeader(Header.IP), context.getHeader(Header.CLIENT_IP)));
        MDCHelper.setChannel(GeneralUtils.coalesce(context.getHeader(Header.CHANNEL)));
        MDCHelper.setUser(GeneralUtils.coalesce(context.getHeader(Header.USER), context.getHeader(Header.USERNAME)));
        this.printLogRequest(context, request);

    }

    public void onFinalProcess(Object response) {
        this.printLogResponse(response);

    }

    private void printLogRequest(HttpServerRequest request, Object body){
        log.info(
            LOG_REQUEST_RECIBIDO,
            request.remoteAddress().toString(),
            request.path(),
            request.headers().entries(),
            Json.encode(body)
        );
    }

    private void printLogResponse(Object response){
        log.info(
            LOG_RESPONSE_ENVIADO,
            Json.encode(response)
        );
    }

}
