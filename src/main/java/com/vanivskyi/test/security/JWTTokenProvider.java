package com.vanivskyi.test.security;

import com.vanivskyi.test.model.User;
import com.vanivskyi.test.exception.JWTAuthenticationException;
import io.jsonwebtoken.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@NoArgsConstructor
@Component
public class JWTTokenProvider {
    private final String SECRET_KEY = CookieProvider.KEY_VALUE_FOR_COOKIE;
    private  final long EXPIRED_TIME_ACCESS_TOKEN = 90000000;

    private JWTUserDetailsService userDetailsService;
    private CookieProvider cookieProvider;

    @Autowired
    public JWTTokenProvider(@Lazy JWTUserDetailsService userDetailsService, CookieProvider cookieProvider) {
        this.userDetailsService = userDetailsService;
        this.cookieProvider = cookieProvider;
    }

    public String generateJWTToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + EXPIRED_TIME_ACCESS_TOKEN))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.resolveUserEmailFromJWT(token));
        return new UsernamePasswordAuthenticationToken(userDetails,
                "", userDetails.getAuthorities());
    }

    public String resolveUserEmailFromJWT(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        return cookieProvider.readCookie(request);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JWTAuthenticationException("JWT token is expired, try to login again.");
        }
    }
}