package com.vanivskyi.test.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserReadDTO {
    private String email;
    private String name;
    private String lastName;
    private LocalDateTime createdAt;
}