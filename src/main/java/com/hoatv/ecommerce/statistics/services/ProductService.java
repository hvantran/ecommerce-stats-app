package com.hoatv.ecommerce.statistics.services;

import com.hoatv.ecommerce.statistics.models.Product;
import com.hoatv.ecommerce.statistics.providers.EMonitorVO;
import com.hoatv.ecommerce.statistics.providers.Tiki;
import com.hoatv.ecommerce.statistics.repositories.ProductRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<EMonitorVO> getAllMonitors(Pageable pageable) {
        List<Product> products = productRepository.findAll(pageable).toList();
        return products.stream().map(product -> new EMonitorVO(product.getMasterId(), product.getProductName(), product.getSubCategory()))
                .collect(Collectors.toList());
    }

    public void addMonitorProduct(EMonitorVO eMonitorVO) {
        tiki.addAdditionalProduct(eMonitorVO);
        productRepository.save(new Product(eMonitorVO.getMasterId(), eMonitorVO.getProductName(), eMonitorVO.getSubCategory()));
    }
}
