package com.example.artship.features.user.mapper;

import org.springframework.stereotype.Component;

import com.example.artship.entities.user.model.User;
import com.example.artship.features.user.dto.UserDto;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(user);
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setUserRole(dto.getUserRole());
        user.setDisplayName(dto.getDisplayName());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setBio(dto.getBio());
        user.setIsPublic(dto.getIsPublic());

        return user;
    }

    public void updateEntityFromDto(UserDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setUserRole(dto.getUserRole());
        user.setDisplayName(dto.getDisplayName());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setBio(dto.getBio());
        user.setIsPublic(dto.getIsPublic());
    }
}