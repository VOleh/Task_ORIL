package com.vanivskyi.test.dto.mapper;

import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserCreateMapper implements BaseMapper<User, UserCreateDTO> {
    @Override
    public UserCreateDTO toDTO(User user) {
        return UserCreateDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .build();
    }

    @Override
    public User toModel(UserCreateDTO userDTO) {
        return User.builder()
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .lastName(userDTO.getLastName())
                .build();
    }
}