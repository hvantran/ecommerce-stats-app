package com.hoatv.controllers;

import com.hoatv.providers.EMonitorVO;
import com.hoatv.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class EcommerceController {

    private final ProductService productService;

    @Autowired
    public EcommerceController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/statistics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMetric(@RequestBody EMonitorVO eMonitorVO) {
        productService.addMonitorProduct(eMonitorVO);
    }
}
