package com.hoatv.ecommerce.statistics.providers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EMonitorVO {

    @Min(value = 1, message = "MasterId cannot be 0")
    private long masterId;

    @NotEmpty(message = "Product name cannot be NULL/empty")
    private String productName;

    @NotEmpty(message = "Subcategory cannot be NULL/empty")
    private String subCategory;
}
