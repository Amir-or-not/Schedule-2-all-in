package com.example.demo.security.services;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user by username/email: {}", username);
        
        // Try to find user by email first (most common case)
        User user = userRepository.findByEmail(username)
            .orElseGet(() -> {
                // If not found by email, try to find by userId
                logger.info("User not found by email, trying userId: {}", username);
                return userRepository.findByUserId(username).orElse(null);
            });
        
        if (user == null) {
            logger.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        logger.info("User found: {} (ID: {}, Email: {}, Role: {})", 
            user.getFullName(), user.getUserId(), user.getEmail(), user.getRole());
        
        return UserDetailsImpl.build(user);
    }
}
