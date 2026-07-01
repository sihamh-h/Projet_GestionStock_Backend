package fr.doranco.GestionStock.mongo.document;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "invalidated_tokens")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private String reason;

    @Indexed(expireAfter = "0s")
    private Date expiresAt;
}
