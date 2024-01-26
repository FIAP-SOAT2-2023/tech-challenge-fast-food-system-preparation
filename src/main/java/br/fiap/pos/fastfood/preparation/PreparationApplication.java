package br.fiap.pos.fastfood.preparation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class PreparationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreparationApplication.class, args);
	}

}
