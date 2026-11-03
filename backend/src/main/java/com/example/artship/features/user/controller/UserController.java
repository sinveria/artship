package com.example.artship.features.user.controller;

import com.example.artship.entities.user.model.User;
import com.example.artship.features.user.dto.UserDto;
import com.example.artship.features.user.mapper.UserMapper;
import com.example.artship.features.user.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(
            UserService userService,
            UserMapper userMapper
    ) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!isValidPagination(page, size)) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<UserDto> users = userService.findPublicUsers(pageable)
                .map(this::toPublicDto);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<UserDto> getPublicUser(
            @PathVariable Long id
    ) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isEmpty()
                || !Boolean.TRUE.equals(userOptional.get().getIsPublic())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toPublicDto(userOptional.get()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id
    ) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isEmpty()
                || !Boolean.TRUE.equals(userOptional.get().getIsPublic())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toPublicDto(userOptional.get()));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(
            @PathVariable String username
    ) {
        Optional<User> userOptional =
                userService.findByUsername(username);

        if (userOptional.isEmpty()
                || !Boolean.TRUE.equals(userOptional.get().getIsPublic())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toPublicDto(userOptional.get()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!isValidPagination(page, size)
                || query == null
                || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<UserDto> users = userService
                .searchByUsername(query.trim(), pageable)
                .map(this::toPublicDto);

        return ResponseEntity.ok(users);
    }

    private UserDto toPublicDto(User user) {
        UserDto dto = userMapper.toDto(user);

        dto.setEmail(null);

        return dto;
    }

    private boolean isValidPagination(int page, int size) {
        return page >= 0 && size >= 1 && size <= 100;
    }
}