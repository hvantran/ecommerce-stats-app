package com.hoatv.services;

import com.hoatv.models.Product;
import com.hoatv.providers.EMonitorVO;
import com.hoatv.providers.Tiki;
import com.hoatv.repositories.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository;
    private final Tiki tiki;

    public ProductService(ProductRepository productRepository, Tiki tiki) {
        this.tiki = tiki;
        this.productRepository = productRepository;
    }

    public void init() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            EMonitorVO monitor = new EMonitorVO(product.getMasterId(), product.getProductName(), product.getSubCategory());
            tiki.addAdditionalProduct(monitor);
        }
    }

    public void addMonitorProduct(EMonitorVO eMonitorVO) {
        tiki.addAdditionalProduct(eMonitorVO);
        productRepository.save(new Product(eMonitorVO.getMasterId(), eMonitorVO.getProductName(), eMonitorVO.getSubCategory()));
    }
}
