package fr.doranco.GestionStock.mongo.document;


import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    private String userEmail;


    private String action;

    private String description;

    private LocalDateTime timestamp;

    private boolean success;

}
