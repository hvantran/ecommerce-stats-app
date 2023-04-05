package com.hoatv.ecommerce.statistics.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private Long masterId;

    @Column
    private String productName;

    @Column
    private String subCategory;
}
