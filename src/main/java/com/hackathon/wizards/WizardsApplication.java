package com.hackathon.wizards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableJpaAuditing
public class WizardsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WizardsApplication.class, args);
	}

}
