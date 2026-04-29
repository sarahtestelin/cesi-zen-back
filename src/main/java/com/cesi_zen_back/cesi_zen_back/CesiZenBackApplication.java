package com.cesi_zen_back.cesi_zen_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CesiZenBackApplication {

  public static void main(String[] args) {
    SpringApplication.run(CesiZenBackApplication.class, args);
  }
}