package com.vanivskyi.test.service;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.dto.UserUpdateDTO;
import com.vanivskyi.test.dto.mapper.UserCreateMapper;
import com.vanivskyi.test.dto.mapper.UserReadMapper;
import com.vanivskyi.test.exception.*;
import com.vanivskyi.test.model.User;
import com.vanivskyi.test.repository.UserRepository;
import com.vanivskyi.test.security.CookieProvider;
import com.vanivskyi.test.security.JWTTokenProvider;
import com.vanivskyi.test.security.UserPrincipal;
import com.vanivskyi.test.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;

@Service
public class UserServiceImpl implements UserService {
    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private UserCreateMapper userCreateMapper;
    private UserReadMapper userReadMapper;
    private PasswordEncoder passwordEncoder;
    private CookieProvider cookieProvider;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserCreateMapper userCreateMapper,
                           UserReadMapper userReadMapper, PasswordEncoder passwordEncoder,
                           CookieProvider cookieProvider, @Lazy JWTTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userCreateMapper = userCreateMapper;
        this.userReadMapper = userReadMapper;
        this.passwordEncoder = passwordEncoder;
        this.cookieProvider = cookieProvider;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Cookie getLogin(AuthenticationRequestDTO requestDTO) {
        String userEmail = requestDTO.getEmail();

        if (this.existsUserByEmail(requestDTO.getEmail())) {

            logger.info(format("User authenticated with email: {0}", userEmail.toUpperCase()));

            if (this.comparePassword(requestDTO)) {

                logger.info(format("User successfully authorized with email :{0}", userEmail));

                User user = userReadMapper.toModel(this.getUser(userEmail));

                return cookieProvider.createCookie(jwtTokenProvider.generateJWTToken(user));

            } else
                throw new AuthorizationException("Password isn't correct");
        } else
            throw new JWTAuthenticationException("User doesn't authenticated try to use registration form.");
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
        Optional<User> user = ofNullable(userRepository.findById(email)).orElseThrow(() ->
                new UserNotFoundException(format("User with {0} not found", email.toUpperCase())));

        return userReadMapper.toDTO(user.get());
    }

    @Override
    public UserReadDTO updateUser(UserUpdateDTO userDTO) {
        User activeUser = this.getCurrentUser();

        logger.info(format("Current active user is: {0}", activeUser));

        User existingUser = userRepository.findById(activeUser.getEmail()).get();

        existingUser.setName(userDTO.getName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));

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
    public boolean comparePassword(AuthenticationRequestDTO requestDto) {
        User user = userRepository.findById(requestDto.getEmail()).get();
        return passwordEncoder.matches(requestDto.getPassword(), user.getPassword());
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
        if (!Validator.validateEmail(userDTO.getEmail())) {
            throw new InvalidEmailException("Email is incorrect");
        }
        if (!Validator.validatePassword(userDTO.getPassword())) {
            throw new InvalidPasswordException("Password is incorrect");
        }
        if (this.existsUserByEmail(userDTO.getEmail())) {
            throw new InvalidUserRegistrationDataException(format("User has already existed with email: {0}",
                    userDTO.getEmail().toUpperCase()));
        }
    }
}