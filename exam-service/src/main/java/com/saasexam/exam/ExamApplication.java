package com.saasexam.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Records exam attempts and scores them against the question bank. */
@SpringBootApplication(scanBasePackages = "com.saasexam")
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }
}
