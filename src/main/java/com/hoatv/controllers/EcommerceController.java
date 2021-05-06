package com.hoatv.controllers;

import com.hoatv.providers.EMonitorVO;
import com.hoatv.providers.Tiki;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class EcommerceController {

    private final Tiki tiki;

    @Autowired
    public EcommerceController(Tiki tiki) {
        this.tiki = tiki;
    }

    @PostMapping(value = "/statistics", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addMetric(@RequestBody EMonitorVO eMonitorVO) {
        tiki.addAdditionalProduct(eMonitorVO.getProductName(), eMonitorVO.getMasterId());
    }
}
