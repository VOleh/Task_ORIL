package com.vanivskyi.test.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDTO {
    private String name;
    private String lastName;
    private String password;
}