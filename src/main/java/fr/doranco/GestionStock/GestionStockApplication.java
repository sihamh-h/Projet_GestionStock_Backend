package fr.doranco.GestionStock;

import fr.doranco.GestionStock.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaAuditing
@EnableScheduling
public class GestionStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionStockApplication.class, args);
	}

}
