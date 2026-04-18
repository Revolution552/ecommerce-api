package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.user.payload.UserResponseDto;
import com.backend.ecommerce.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")  // 👈 Only ADMIN can access
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsersForAdmin();
    }

}