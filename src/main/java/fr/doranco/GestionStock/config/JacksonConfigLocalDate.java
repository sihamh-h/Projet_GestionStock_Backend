package fr.doranco.GestionStock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Configuration personnalisée de Jackson pour gérer la sérialisation et désérialisation
 * des objets LocalDate au format "dd-MM-yyyy".
 */
@Configuration
public class JacksonConfigLocalDate {

    private static final String DATE_FORMAT = "dd-MM-yyyy";

    @Bean
    public ObjectMapper objectMapper() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(
                LocalDate.class,
                new LocalDateDeserializer(dateFormatter)
        );
        javaTimeModule.addSerializer(
                LocalDate.class,
                new LocalDateSerializer(dateFormatter)
        );

        return JsonMapper.builder()
                .addModule(javaTimeModule)
                .build();
    }
}
