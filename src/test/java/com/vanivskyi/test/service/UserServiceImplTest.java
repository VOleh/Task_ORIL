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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private final static String EMAIL = "email@gmail.com";
    private final static String INCORRECT_EMAIL = "email";
    private final static String PASSWORD = "password";
    private final static String INCORRECT_PASSWORD = "pa";

    @InjectMocks
    private UserServiceImpl underTest;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCreateMapper userCreateMapper;
    @Mock
    private UserReadMapper userReadMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CookieProvider cookieProvider;
    @Mock
    private JWTTokenProvider jwtTokenProvider;


    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetLoginWhenUserExistAndPasswordCorrect() {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO();
        requestDTO.setEmail(EMAIL);
        requestDTO.setPassword(PASSWORD);

        Cookie expectedCookie = new Cookie(CookieProvider.KEY_VALUE_FOR_COOKIE, "TOKEN");
        expectedCookie.setMaxAge(CookieProvider.EXPIRED_TIME_FOR_COOKIE);

        User existUser = buildUser(PASSWORD);

        UserReadDTO userReadDTO = getUserReadDTO(existUser);
        User userFromDTO = getUserFromDTO(userReadDTO);

        when(userRepository.existsById(requestDTO.getEmail())).thenReturn(true);
        when(userRepository.findById(requestDTO.getEmail())).thenReturn(Optional.ofNullable(existUser));
        when(passwordEncoder.matches(requestDTO.getPassword(), existUser.getPassword())).thenReturn(true);
        when(userRepository.findById(requestDTO.getEmail())).thenReturn(Optional.ofNullable(existUser));
        when(userReadMapper.toDTO(existUser)).thenReturn(userReadDTO);
        when(userReadMapper.toModel(userReadDTO)).thenReturn(userFromDTO);
        when(jwtTokenProvider.generateJWTToken(userFromDTO)).thenReturn("TOKEN");
        when(cookieProvider.createCookie("TOKEN")).thenReturn(expectedCookie);

        Cookie result = underTest.getLogin(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedCookie);
        assertThat(result.getName()).isEqualTo(CookieProvider.KEY_VALUE_FOR_COOKIE);
        assertThat(result.getValue()).isEqualTo("TOKEN");

        verify(userRepository).existsById(requestDTO.getEmail());
        verify(userRepository, times(2)).findById(requestDTO.getEmail());
        verify(passwordEncoder).matches(requestDTO.getPassword(), existUser.getPassword());
        verify(userReadMapper).toDTO(existUser);
        verify(userReadMapper).toModel(userReadDTO);
        verify(jwtTokenProvider).generateJWTToken(userFromDTO);
        verify(cookieProvider).createCookie("TOKEN");
    }

    @Test(expectedExceptions = JWTAuthenticationException.class)
    public void testGetLoginWhenUserDoesntAuthenticated() {

        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO();
        requestDTO.setEmail(EMAIL);
        requestDTO.setPassword(PASSWORD);

        when(userRepository.existsById(requestDTO.getEmail())).thenReturn(false);

        underTest.getLogin(requestDTO);

        verify(userRepository).existsById(requestDTO.getEmail());
    }

    @Test(expectedExceptions = AuthorizationException.class)
    public void testGetLoginWhenUserExistAndPasswordIncorrect() {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO();
        requestDTO.setEmail(EMAIL);
        requestDTO.setPassword(INCORRECT_PASSWORD);

        User existUser = buildUser(INCORRECT_PASSWORD);

        when(userRepository.existsById(requestDTO.getEmail())).thenReturn(true);
        when(userRepository.findById(requestDTO.getEmail())).thenReturn(Optional.ofNullable(existUser));
        when(passwordEncoder.matches(requestDTO.getPassword(), existUser.getPassword())).thenReturn(false);

        underTest.getLogin(requestDTO);

        verify(userRepository).existsById(requestDTO.getEmail());
        verify(userRepository).findById(requestDTO.getEmail());
        verify(passwordEncoder).matches(requestDTO.getPassword(), existUser.getPassword());
    }

    @Test
    public void testSaveUser() {
        UserCreateDTO userCreateDTO = getUserCreatDTO(EMAIL, PASSWORD);
        User user = buildUser(PASSWORD);
        UserReadDTO userReadDTO = getUserReadDTO(user);

        when(userCreateMapper.toModel(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn(PASSWORD);
        when(userRepository.save(user)).thenReturn(user);
        when(userReadMapper.toDTO(user)).thenReturn(userReadDTO);

        UserReadDTO result = underTest.saveUser(userCreateDTO);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userReadDTO);

        verify(userCreateMapper).toModel(userCreateDTO);
        verify(passwordEncoder).encode(userCreateDTO.getPassword());
        verify(userRepository).save(user);
        verify(userReadMapper).toDTO(user);
    }

    @Test
    public void testGetUser() {
        User existUser = buildUser(PASSWORD);

        UserReadDTO userDTO = getUserReadDTO(existUser);

        when(userRepository.findById(EMAIL)).thenReturn(Optional.ofNullable(existUser));
        when(userReadMapper.toDTO(existUser)).thenReturn(userDTO);

        UserReadDTO result = underTest.getUser(EMAIL);

        verify(userRepository).findById(EMAIL);
        verify(userReadMapper).toDTO(existUser);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
    }


    @Test(expectedExceptions = UserNotFoundException.class)
    public void testGetUserWhenUserDoesntExist() {
        when(userRepository.findById(EMAIL)).thenReturn(null);

        underTest.getUser(EMAIL);

        verify(userRepository).findById(EMAIL);
    }

    @Test
    public void testUpdateUser() {
        UserUpdateDTO userUpdateDTO = getUserUpdateDTO("newName", "newLastName", "newPassword");
        User userContext = buildUser(PASSWORD);
        UserDetails userDetails = new UserPrincipal(userContext);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
                "", userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        User currentUserDB = buildUser(PASSWORD);

        User updatedUser = buildUser(PASSWORD);
        updatedUser.setName("newName");
        updatedUser.setLastName("newLastName");
        updatedUser.setPassword("newPassword");

        UserReadDTO userReadDTO = getUserReadDTO(updatedUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(userRepository.findById(userDetails.getUsername())).thenReturn(Optional.ofNullable(currentUserDB));
        when(passwordEncoder.encode(userUpdateDTO.getPassword())).thenReturn(userUpdateDTO.getPassword());
        when(userRepository.save(currentUserDB)).thenReturn(updatedUser);
        when(userReadMapper.toDTO(updatedUser)).thenReturn(userReadDTO);


        UserReadDTO result = underTest.updateUser(userUpdateDTO);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userReadDTO);

        verify(securityContext).getAuthentication();
        verify(SecurityContextHolder.getContext()).getAuthentication();
        verify(userRepository).findById(userDetails.getUsername());
        verify(passwordEncoder).encode(userUpdateDTO.getPassword());
        verify(userRepository).save(currentUserDB);
        verify(userReadMapper).toDTO(updatedUser);
    }

    @Test
    public void testDeleteUser() {
        doNothing().when(userRepository).deleteById(EMAIL);

        String result = underTest.deleteUser(EMAIL);
        assertThat(result).isEqualTo(format("User with email {0} successfully " +
                "removed from Data Base", EMAIL.toUpperCase()));

        verify(userRepository).deleteById(EMAIL);
    }

    @Test
    public void testGetAllUsers() {
        User user = buildUser(PASSWORD);
        List<User> users = Collections.singletonList(user);
        UserReadDTO userDTO = getUserReadDTO(user);

        when(userRepository.findAll()).thenReturn(users);
        when(userReadMapper.toDTO(user)).thenReturn(userDTO);

        List<UserReadDTO> result = underTest.getAllUsers();
        assertThat(result).isNotEmpty();
        assertThat(result).contains(userDTO);

        verify(userRepository).findAll();
        verify(userReadMapper).toDTO(user);
    }

    @Test
    public void testExistsUserByEmail() {
        when(userRepository.existsById(EMAIL)).thenReturn(true);
        boolean result = underTest.existsUserByEmail(EMAIL);
        assertThat(result).isTrue();
        verify(userRepository).existsById(EMAIL);

    }

    @Test
    public void testComparePassword() {
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO();
        requestDTO.setEmail(EMAIL);
        requestDTO.setPassword(PASSWORD);

        User user = buildUser(PASSWORD);

        when(userRepository.findById(requestDTO.getEmail())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.matches(requestDTO.getPassword(), user.getPassword())).thenReturn(true);

        boolean result = underTest.comparePassword(requestDTO);
        assertThat(result).isTrue();

        verify(userRepository).findById(requestDTO.getEmail());
        verify(passwordEncoder).matches(requestDTO.getPassword(), user.getPassword());

    }

    @Test(expectedExceptions = InvalidEmailException.class)
    public void testValidateRegistrationRequestWhenEmailInvalid() {
        UserCreateDTO userDTO = getUserCreatDTO(INCORRECT_EMAIL, PASSWORD);
        underTest.validateRegistrationRequest(userDTO);
    }

    @Test(expectedExceptions = InvalidPasswordException.class)
    public void testValidateRegistrationRequestWhenPasswordInvalid() {
        UserCreateDTO userDTO = getUserCreatDTO(EMAIL, INCORRECT_PASSWORD);
        underTest.validateRegistrationRequest(userDTO);
    }

    @Test(expectedExceptions = InvalidUserRegistrationDataException.class)
    public void testValidateRegistrationRequestWhenEmailAlreadyExisted() {
        UserCreateDTO userDTO = getUserCreatDTO(EMAIL, PASSWORD);

        when(userRepository.existsById(userDTO.getEmail())).thenReturn(true);

        underTest.validateRegistrationRequest(userDTO);

        verify(userRepository).existsById(userDTO.getEmail());
    }

    private User buildUser(String password) {
        return User.builder()
                .email(EMAIL)
                .password(password)
                .build();
    }

    private UserReadDTO getUserReadDTO(User user) {
        return UserReadDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .build();
    }

    private User getUserFromDTO(UserReadDTO userDTO) {
        return User.builder()
                .email(userDTO.getEmail())
                .build();
    }

    private UserCreateDTO getUserCreatDTO(String email, String password) {
        return UserCreateDTO.builder()
                .email(email)
                .password(password)
                .build();
    }

    private UserUpdateDTO getUserUpdateDTO(String name, String lastName, String password) {
        return UserUpdateDTO.builder()
                .name(name)
                .lastName(lastName)
                .password(password)
                .build();
    }
}