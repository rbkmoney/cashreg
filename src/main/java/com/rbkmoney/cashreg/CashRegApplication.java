package com.rbkmoney.cashreg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.cashreg"})
public class CashRegApplication {
    public static void main(String[] args) {
        SpringApplication.run(CashRegApplication.class, args);
    }
}
