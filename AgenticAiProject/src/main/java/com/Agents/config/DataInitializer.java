package com.Agents.config;

import com.Agents.Entity.Product;
import com.Agents.OrderRepository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product("laptop", 999.99));
                productRepository.save(new Product("phone", 699.99));
                productRepository.save(new Product("mobile", 699.99));
                productRepository.save(new Product("headphones", 149.99));
                productRepository.save(new Product("mouse", 29.99));
                productRepository.save(new Product("keyboard", 49.99));
                productRepository.save(new Product("monitor", 249.99));
            }
        };
    }
}