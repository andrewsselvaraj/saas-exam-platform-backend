package com.saasexam.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Owns questions and their answer options. Refers to the catalog's subjects and chapters by id only. */
@SpringBootApplication(scanBasePackages = "com.saasexam")
public class QuestionBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);
    }
}
