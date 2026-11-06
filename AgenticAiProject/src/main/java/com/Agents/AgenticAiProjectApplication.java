package com.Agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgenticAiProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgenticAiProjectApplication.class, args);
		System.out.println("working start");
	}

}
