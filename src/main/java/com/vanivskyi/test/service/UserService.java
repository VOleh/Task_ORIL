package com.vanivskyi.test.service;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.model.User;

import java.util.List;

public interface UserService {

    UserReadDTO saveUser(UserCreateDTO type);

    UserReadDTO getUser(String email);

    UserReadDTO updateUser(String name, String lastName, String password);

    String deleteUser(String email);

    List<UserReadDTO> getAllUsers();

    boolean existsUserByEmail(String email);

    boolean comparePasswordLogin(AuthenticationRequestDTO requestDto);

    void validateRegistrationRequest(UserCreateDTO userCreateDTO);

    User getCurrentUser();
}