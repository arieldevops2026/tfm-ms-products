package dev.ariel.tfm.products.repository;

import dev.ariel.tfm.products.api.dto.AmountObj;
import dev.ariel.tfm.products.api.dto.Product;
import dev.ariel.tfm.products.util.constants.Currency;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Catalogo en memoria. Los datos son efimeros y de solo lectura: se cargan al iniciar el servicio.
 */
@ApplicationScoped
public class ProductRepository {

    private final Map<String, Product> products = Stream.of(
            product("PRD-001", "Laptop 14 pulgadas", "TECNOLOGIA", "850.00"),
            product("PRD-002", "Monitor 27 pulgadas", "TECNOLOGIA", "280.00"),
            product("PRD-003", "Teclado mecanico", "TECNOLOGIA", "75.50"),
            product("PRD-004", "Auriculares inalambricos", "TECNOLOGIA", "120.00"),
            product("PRD-005", "Cafetera de goteo", "HOGAR", "45.90"),
            product("PRD-006", "Lampara de escritorio", "HOGAR", "32.00"),
            product("PRD-007", "Silla ergonomica", "HOGAR", "210.00"),
            product("PRD-008", "Balon de futbol", "DEPORTES", "25.00"),
            product("PRD-009", "Raqueta de tenis", "DEPORTES", "98.75"),
            product("PRD-010", "Bicicleta de montana", "DEPORTES", "430.00"),
            product("PRD-011", "Libro de arquitectura de software", "LIBROS", "39.99"),
            product("PRD-012", "Libro de practicas DevOps", "LIBROS", "34.50"))
        .collect(Collectors.toMap(Product::getProductId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(products.get(productId));
    }

    private static Product product(String productId, String name, String category, String price) {
        return Product.builder()
            .productId(productId)
            .name(name)
            .category(category)
            .price(AmountObj.builder()
                .currency(Currency.USD)
                .amount(new BigDecimal(price))
                .build())
            .build();
    }

}
