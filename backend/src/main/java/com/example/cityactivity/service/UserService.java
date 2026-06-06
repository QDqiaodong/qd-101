package com.example.cityactivity.service;

import com.example.cityactivity.dto.response.ActivityFootprintDTO;
import com.example.cityactivity.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findById(Long id);
    User createUser(User user);
    Optional<User> findByUsername(String username);
    List<ActivityFootprintDTO> getUserActivityFootprints(Long userId);
}
