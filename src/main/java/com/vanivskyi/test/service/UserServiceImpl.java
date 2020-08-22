package com.vanivskyi.test.service;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.dto.mapper.UserCreateMapper;
import com.vanivskyi.test.dto.mapper.UserReadMapper;
import com.vanivskyi.test.exception.InvalidUserRegistrationDataException;
import com.vanivskyi.test.exception.UserNotFoundException;
import com.vanivskyi.test.model.User;
import com.vanivskyi.test.repository.UserRepository;
import com.vanivskyi.test.security.UserPrincipal;
import com.vanivskyi.test.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Service
public class UserServiceImpl implements UserService {
    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private UserCreateMapper userCreateMapper;
    private UserReadMapper userReadMapper;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserCreateMapper userCreateMapper,
                           UserReadMapper userReadMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userCreateMapper = userCreateMapper;
        this.userReadMapper = userReadMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserReadDTO saveUser(UserCreateDTO userDTO) {
        User user = userCreateMapper.toModel(userDTO);

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        logger.info(format("Saved user {0}", savedUser));

        return userReadMapper.toDTO(savedUser);
    }

    @Override
    public UserReadDTO getUser(String email) {
        return userRepository.findById(email).map(val -> userReadMapper.toDTO(val))
                .orElseThrow(() ->
                        new UserNotFoundException(format("User with {0} not found", email.toUpperCase())));
    }

    @Override
    public UserReadDTO updateUser(String name, String lastName, String password) {
        User activeUser = this.getCurrentUser();

        logger.info(format("Current active user is: {0}", activeUser));

        User existingUser = userRepository.findById(activeUser.getEmail()).get();

        existingUser.setName(name);
        existingUser.setLastName(lastName);
        existingUser.setPassword(passwordEncoder.encode(password));

        User updatedUser = userRepository.save(existingUser);

        logger.info(format("Updated user is: {0}", updatedUser));

        return userReadMapper.toDTO(updatedUser);
    }

    @Override
    public String deleteUser(String email) {
        userRepository.deleteById(email);
        return format("User with email {0} successfully removed from Data Base", email.toUpperCase());
    }

    @Override
    public List<UserReadDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> userReadMapper.toDTO(user))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsUserByEmail(String email) {
        return userRepository.existsById(email);
    }

    @Override
    public boolean comparePasswordLogin(AuthenticationRequestDTO requestDto) {
        User user = userRepository.findById(requestDto.getEmail()).get();
        return user.getPassword().equals(passwordEncoder.encode(requestDto.getPassword()));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        logger.info(format("UserPrincipal is: {0}", principal));

        return principal.getUser();
    }

    @Override
    public void validateRegistrationRequest(UserCreateDTO userDTO) {
        if (!Validator.validateEmail(userDTO.getEmail())
                || !Validator.validatePassword(userDTO.getPassword())
                || this.existsUserByEmail(userDTO.getEmail())) {
            throw new InvalidUserRegistrationDataException("Password and email aren't valid. Please check data again.");
        } else return;
    }
}