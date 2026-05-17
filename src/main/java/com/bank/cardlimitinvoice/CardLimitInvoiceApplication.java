package com.bank.cardlimitinvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardLimitInvoiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardLimitInvoiceApplication.class, args);
    }
}
