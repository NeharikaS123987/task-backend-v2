package com.example.taskmanager.security;

import com.example.taskmanager.user.User;
import com.example.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = users.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found: " + email));
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(), u.getPasswordHash(), authorities
        );
    }
}