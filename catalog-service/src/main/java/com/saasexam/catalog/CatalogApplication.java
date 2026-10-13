package com.saasexam.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Owns the school hierarchy: organisations, classes, subjects and chapters. */
@SpringBootApplication(scanBasePackages = "com.saasexam")
public class CatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
