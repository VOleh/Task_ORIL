package com.vanivskyi.test.controller;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.dto.UserReadDTO;
import com.vanivskyi.test.dto.mapper.UserCreateMapper;
import com.vanivskyi.test.dto.mapper.UserReadMapper;
import com.vanivskyi.test.model.User;
import com.vanivskyi.test.security.CookieProvider;
import com.vanivskyi.test.security.JWTTokenProvider;
import com.vanivskyi.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/test/app")
public class UserController {

    private AuthenticationManager authenticationManager;
    private UserService userService;
    private CookieProvider cookieProvider;
    private JWTTokenProvider jwtTokenProvider;
    private UserReadMapper userReadMapper;

    @Autowired
    public UserController(AuthenticationManager authenticationManager,
                          UserService userService, CookieProvider cookieProvider,
                          JWTTokenProvider jwtTokenProvider, UserReadMapper userReadMapper) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.cookieProvider = cookieProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userReadMapper = userReadMapper;
    }


    @PostMapping("/registration")
    public HttpStatus registrationUser(@RequestBody UserCreateDTO userDTO) {
        userService.validateRegistrationRequest(userDTO);
        userService.saveUser(userDTO);

        return HttpStatus.OK;
    }

    @PostMapping("/sign-in")
    public void getLogin(@RequestBody AuthenticationRequestDTO requestDTO, HttpServletResponse response) {
        String userEmail = requestDTO.getEmail();
        try {
            if (userService.existsUserByEmail(userEmail)) {

                if (userService.comparePasswordLogin(requestDTO)) {
                    authenticationManager.
                            authenticate(new UsernamePasswordAuthenticationToken(userEmail, requestDTO.getPassword()));
                }

                User user = userReadMapper.toModel(userService.getUser(userEmail));

                Cookie cookie = cookieProvider.createCookie(jwtTokenProvider.generateJWTToken(user));

                response.addCookie(cookie);
            }
        } catch (RuntimeException ex) {
//            response.sendError(404, " " + userEmail);
//        } catch (RuntimeException ex) {
//            response.sendError(400, "");
//        }
    }
}

    @PostMapping("/create")
    public HttpStatus createUser(@RequestBody UserCreateDTO userDTO) {
        userService.validateRegistrationRequest(userDTO);
        userService.saveUser(userDTO);

        return HttpStatus.OK;
    }

    @GetMapping("/get{email}")
    public UserReadDTO getUser(@PathVariable("email") String email) {
        return userService.getUser(email);
    }

    @GetMapping("/getUsers")
    public List<UserReadDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/update")
    public UserReadDTO updateUser(@RequestParam("name") String name,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("password") String password) {

        return userService.updateUser(name, lastName, password);
    }
}