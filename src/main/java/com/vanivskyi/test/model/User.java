package com.vanivskyi.test.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Document(collection = "Users")
public class User {
    @Id
    private String email;
    private String name;
    private String lastName;
    private String password;
    private LocalDateTime createdAt;

}