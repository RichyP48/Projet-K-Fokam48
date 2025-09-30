package com.mogou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@EnableFeignClients
public class ConventionsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConventionsServiceApplication.class, args);
	}

}
