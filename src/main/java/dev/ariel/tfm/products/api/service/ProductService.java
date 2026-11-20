package dev.ariel.tfm.products.api.service;

import dev.ariel.tfm.products.api.dto.GetProductsRequest;
import dev.ariel.tfm.products.api.dto.Product;
import dev.ariel.tfm.products.repository.ProductRepository;
import dev.ariel.tfm.products.util.fault.FaultInjector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ProductService {

    private final ProductRepository repository;
    private final FaultInjector faultInjector;

    public List<Product> getProducts(GetProductsRequest request) {
        faultInjector.apply();
        log.info("Obteniendo lista de productos, categoria: {}, pagina: {}, maxRecords: {}",
            request.getCategory(), request.getPageNumber(), request.getMaxRecords());

        return repository.findAll().stream()
            .filter(product -> matchesCategory(product, request.getCategory()))
            .skip((long) (request.getPageNumber() - 1) * request.getMaxRecords())
            .limit(request.getMaxRecords())
            .toList();
    }

    public Product getProductById(String productId) {
        faultInjector.apply();
        log.info("Obteniendo producto con id: {}", productId);

        return repository.findById(productId)
            .orElseThrow(() -> {
                log.warn("Producto no encontrado, id: {}", productId);
                return new NotFoundException("Producto no encontrado: " + productId);
            });
    }

    private boolean matchesCategory(Product product, String category) {
        return category == null || category.isBlank() || product.getCategory().equalsIgnoreCase(category.trim());
    }

}
