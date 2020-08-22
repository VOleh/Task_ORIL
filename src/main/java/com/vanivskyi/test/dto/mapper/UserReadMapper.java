package com.vanivskyi.test.dto.mapper;

import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements BaseMapper<User, UserReadDTO> {
    @Override
    public UserReadDTO toDTO(User user) {
        return UserReadDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public User toModel(UserReadDTO userDTO) {
        return User.builder()
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .lastName(userDTO.getLastName())
                .createdAt(userDTO.getCreatedAt())
                .build();
    }
}