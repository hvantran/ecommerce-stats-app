package com.hoatv.ecommerce.statistics.controllers;

import com.hoatv.ecommerce.statistics.providers.EMonitorVO;
import com.hoatv.ecommerce.statistics.services.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class EcommerceController {

    private final ProductService productService;

    public EcommerceController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/statistics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addProductMonitor(@Valid @RequestBody EMonitorVO eMonitorVO) {
        productService.addMonitorProduct(eMonitorVO);
    }

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EMonitorVO> getAllMonitorProducts(@RequestParam int pageIndex, @RequestParam int pageSize) {
        return productService.getAllMonitors(PageRequest.of(pageIndex, pageSize));
    }
}
