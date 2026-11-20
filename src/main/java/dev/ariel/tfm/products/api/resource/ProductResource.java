package dev.ariel.tfm.products.api.resource;

import dev.ariel.tfm.products.api.dto.GetProductsRequest;
import dev.ariel.tfm.products.api.dto.Product;
import dev.ariel.tfm.products.api.service.ProductService;
import dev.ariel.tfm.products.api.service.RequestService;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import java.util.List;

/**
 * Resource para operaciones de consulta del catalogo de productos
 */
@Slf4j
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProductResource {

    private final ProductService service;
    private final RequestService requestService;

    /**
     * Endpoint para obtener la lista de productos paginada
     */
    @POST
    @Path("/get-list")
    @RunOnVirtualThread
    public List<Product> getList(@Context HttpServerRequest context, @NotNull @Valid GetProductsRequest request) {
        requestService.onInitialProcess(context, request);

        List<Product> products = service.getProducts(request);

        requestService.onFinalProcess(products);
        return products;
    }

    /**
     * Endpoint para obtener un producto por su id
     */
    @GET
    @Path("/get-by-id/{id}")
    @RunOnVirtualThread
    public Product getById(@Context HttpServerRequest context, @PathParam("id") String id) {
        requestService.onInitialProcess(context, id);

        Product product = service.getProductById(id);

        requestService.onFinalProcess(product);
        return product;
    }

}
