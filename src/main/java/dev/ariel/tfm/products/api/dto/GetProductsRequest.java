package dev.ariel.tfm.products.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para solicitud de lista de productos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetProductsRequest {

    @NotNull(message = "pageNumber is required")
    @Min(value = 1, message = "pageNumber must be greater than zero")
    @JsonProperty("pageNumber")
    private Integer pageNumber;

    @NotNull(message = "maxRecords is required")
    @Min(value = 1, message = "maxRecords must be greater than zero")
    @Max(value = 100, message = "maxRecords must not exceed 100")
    @JsonProperty("maxRecords")
    private Integer maxRecords;

    /**
     * Filtro opcional por categoria
     */
    @JsonProperty("category")
    private String category;
}
