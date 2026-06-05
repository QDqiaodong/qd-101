package com.example.cityactivity.service;

import com.example.cityactivity.entity.User;

import java.util.Optional;

public interface UserService {
    User findById(Long id);
    User createUser(User user);
    Optional<User> findByUsername(String username);
}
