package com.example.demo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.security.services.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.debug("JWT Token found: {}", jwt);
                
                if (jwtUtils.validateJwtToken(jwt)) {
                    String email = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("JWT Token validated for user: {}", email);
                    
                    // Get roles from the token
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtUtils.key())
                            .build()
                            .parseClaimsJws(jwt)
                            .getBody();
                            
                    // Get the username from the token
                    String username = claims.getSubject();
                    
                    // Get roles from the token
                    String roles = claims.get("roles", String.class);
                    logger.debug("Roles from token: {}", roles);
                    
                    // If no roles in token, try to load from database
                    Collection<? extends GrantedAuthority> authorities;
                    if (roles != null && !roles.isEmpty()) {
                        // Create authorities from roles in the token
                        authorities = Arrays.stream(roles.split(","))
                                .map(role -> new SimpleGrantedAuthority(role.trim()))
                                .collect(Collectors.toList());
                    } else {
                        // Fallback to loading user details from database
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        authorities = userDetails.getAuthorities();
                        logger.debug("No roles in token, loaded from database: {}", authorities);
                    }
                    
                    // Create authentication with the extracted authorities
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set in SecurityContext with roles: {}", roles);
                } else {
                    logger.warn("JWT Token validation failed");
                }
            } else {
                logger.debug("No JWT token found in request headers");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized - " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
