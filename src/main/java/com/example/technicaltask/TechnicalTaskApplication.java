package com.example.technicaltask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
public class TechnicalTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechnicalTaskApplication.class, args);
    }

}
