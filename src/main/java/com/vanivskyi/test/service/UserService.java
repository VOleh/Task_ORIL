package com.vanivskyi.test.service;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.dto.UserUpdateDTO;
import com.vanivskyi.test.model.User;

import javax.servlet.http.Cookie;
import java.util.List;

public interface UserService {

    UserReadDTO saveUser(UserCreateDTO type);

    UserReadDTO getUser(String email);

    UserReadDTO updateUser(UserUpdateDTO userDTO);

    String deleteUser(String email);

    List<UserReadDTO> getAllUsers();

    boolean existsUserByEmail(String email);

    boolean comparePassword(AuthenticationRequestDTO requestDto);

    void validateRegistrationRequest(UserCreateDTO userCreateDTO);

    Cookie getLogin(AuthenticationRequestDTO requestDTO);

    User getCurrentUser();
}