package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LedgerServerApplication {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You need to provide a bft id using command line argument");
            System.exit(-1);
        }

        SpringApplication.run(LedgerServerApplication.class, args);
    }
}
