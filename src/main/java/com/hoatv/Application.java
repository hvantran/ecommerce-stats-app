package com.hoatv;

import com.hoatv.providers.Tiki;
import com.hoatv.repositories.ProductRepository;
import com.hoatv.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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
