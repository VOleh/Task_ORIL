package com.vanivskyi.test.security;

import com.vanivskyi.test.dto.mapper.UserReadMapper;
import com.vanivskyi.test.model.User;
import com.vanivskyi.test.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static java.text.MessageFormat.format;

@Component
public class JWTUserDetailsService implements UserDetailsService {
    private UserService userService;
    private UserReadMapper userReadMapper;

    Logger logger = LoggerFactory.getLogger(JWTUserDetailsService.class);

    @Autowired
    public JWTUserDetailsService(UserService userService, UserReadMapper userReadMapper) {
        this.userService = userService;
        this.userReadMapper = userReadMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User currentUser = userReadMapper.toModel(userService.getUser(userEmail));

        logger.info(format("Authenticated user is: {0}", currentUser));

        return new UserPrincipal(currentUser);
    }
}