package com.hoatv.controllers;

import com.hoatv.providers.EMonitorVO;
import com.hoatv.services.ProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class EcommerceController {

    private final ProductService productService;

    public EcommerceController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/statistics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMetric(@RequestBody EMonitorVO eMonitorVO) {
        productService.addMonitorProduct(eMonitorVO);
    }

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EMonitorVO> getAllMonitorProducts() {
        return productService.getAllMonitors();
    }
}
