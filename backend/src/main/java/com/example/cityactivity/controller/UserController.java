package com.example.cityactivity.controller;

import com.example.cityactivity.dto.response.ActivityFootprintDTO;
import com.example.cityactivity.dto.response.ApiResponse;
import com.example.cityactivity.entity.User;
import com.example.cityactivity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(ApiResponse.success("User created", created));
    }
    
    @GetMapping("/{id}/footprints")
    public ResponseEntity<ApiResponse<List<ActivityFootprintDTO>>> getUserActivityFootprints(@PathVariable Long id) {
        List<ActivityFootprintDTO> footprints = userService.getUserActivityFootprints(id);
        return ResponseEntity.ok(ApiResponse.success(footprints));
    }
}
