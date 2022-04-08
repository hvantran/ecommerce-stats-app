package com.hoatv;

import com.hoatv.providers.Tiki;
import com.hoatv.repositories.ProductRepository;
import com.hoatv.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ProductService getTikiProductService(ProductRepository productRepository, Tiki tiki){
        return new ProductService(productRepository, tiki);
    }

    public CommandLineRunner commandLineRunner(ApplicationContext ctx){
        return args -> {
            ProductService productService = ctx.getBean(ProductService.class);
            productService.init();
        };
    }
}
