package com.hoatv.ecommerce.statistics;

import com.hoatv.ecommerce.statistics.providers.Tiki;
import com.hoatv.ecommerce.statistics.repositories.ProductRepository;
import com.hoatv.ecommerce.statistics.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@EnableAutoConfiguration
@ComponentScan({"com.hoatv.ecommerce.statistics", "com.hoatv.springboot.common"})
public class EcommerceStatisticsApplication {


    public static void main(String[] args) {
        SpringApplication.run(EcommerceStatisticsApplication.class, args);
    }

    @Bean
    public Tiki getTikiBean(){
        return new Tiki();
    }

    @Bean
    public ProductService getTikiProductService(ProductRepository productRepository, Tiki tiki){
        return new ProductService(productRepository, tiki);
    }

    @Bean
    @Order
    public CommandLineRunner commandLineRunner(ProductService productService){
        return args -> productService.init();
    }
}
