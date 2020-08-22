package com.vanivskyi.test.security;

import com.vanivskyi.test.dto.mapper.UserReadMapper;
import com.vanivskyi.test.model.User;
import com.vanivskyi.test.security.exception.JWTAuthenticationException;
import com.vanivskyi.test.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;

@Component
public class JWTUserDetailsService implements UserDetailsService {
    Logger logger = LoggerFactory.getLogger(JWTUserDetailsService.class);
    private UserService userService;
    private UserReadMapper userReadMapper;

    @Autowired
    public JWTUserDetailsService(UserService userService, UserReadMapper userReadMapper) {
        this.userService = userService;
        this.userReadMapper = userReadMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        logger.info(format("Authentication by email: {0}", userEmail));

        User currentUser = userReadMapper.toModel(userService.getUser(userEmail));

        logger.info(format("Authenticated user is: {0}", currentUser));

        return ofNullable(currentUser)
                .map(user -> new UserPrincipal(currentUser))
                .orElseThrow(() ->
                        new JWTAuthenticationException(format("User doesn't authenticated by thia email: {0}" +
                                userEmail.toUpperCase())));
    }
}