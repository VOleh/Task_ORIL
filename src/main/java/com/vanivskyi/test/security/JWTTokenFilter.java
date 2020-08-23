package com.vanivskyi.test.security;

import com.vanivskyi.test.exception.JWTAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.text.MessageFormat.format;

public class JWTTokenFilter extends GenericFilterBean {

    Logger logger = LoggerFactory.getLogger(JWTTokenFilter.class);
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public JWTTokenFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String accessToken = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);

        logger.info(format("Access token is: {0}", accessToken));

        if (accessToken != null) {
            try {
                if (jwtTokenProvider.validateToken(accessToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

                    logger.info(format("Authentication is: {0}", authentication.isAuthenticated()));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    ((HttpServletResponse) servletResponse).sendError(403, "JWT token expired");
                }

            } catch (JWTAuthenticationException e) {
                ((HttpServletResponse) servletResponse).sendError(400);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}