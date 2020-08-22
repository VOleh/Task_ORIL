package com.vanivskyi.test.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserCreateDTO {
    private String email;
    private String name;
    private String lastName;
    private String password;
}