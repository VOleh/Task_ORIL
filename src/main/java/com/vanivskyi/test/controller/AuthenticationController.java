package com.vanivskyi.test.controller;

import com.vanivskyi.test.dto.AuthenticationRequestDTO;
import com.vanivskyi.test.dto.UserCreateDTO;
import com.vanivskyi.test.exception.AuthorizationException;
import com.vanivskyi.test.exception.JWTAuthenticationException;
import com.vanivskyi.test.security.CookieProvider;
import com.vanivskyi.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.text.MessageFormat.format;

@RestController
@RequestMapping("test/app")
public class AuthenticationController {
    @Autowired
    private UserService userService;
    @Autowired
    private CookieProvider cookieProvider;


    @PostMapping("/registration")
    public HttpStatus registrationUser(@RequestBody UserCreateDTO userDTO) {
        userService.validateRegistrationRequest(userDTO);
        userService.saveUser(userDTO);

        return HttpStatus.OK;
    }

    @PostMapping("/login")
    public void getLogin(@RequestBody AuthenticationRequestDTO requestDTO, HttpServletResponse response) throws IOException {
        try {
            Cookie cookie = userService.getLogin(requestDTO);
            response.addCookie(cookie);

        } catch (JWTAuthenticationException ex) {
            response.sendError(404, format("User with this email {0} doesn't exist", requestDTO.getEmail().toUpperCase()));
        } catch (AuthorizationException ex) {
            response.sendError(400, "Password is incorrect");
        }
    }

    @GetMapping("/logout")
    public HttpStatus getLogout(HttpServletResponse response) {
        response.addCookie(cookieProvider.deleteCookie());
        return HttpStatus.OK;
    }
}