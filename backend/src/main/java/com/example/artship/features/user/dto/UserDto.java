package com.example.artship.features.user.dto;

import java.time.LocalDateTime;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;

public class UserDto {

    private Long id;
    private String username;
    private String email;
    private UserRole userRole;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;
    private LocalDateTime createdAt;

    public UserDto() {
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.userRole = user.getUserRole();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.isPublic = user.getIsPublic();
        this.createdAt = user.getCreatedAt();
    }

    public UserDto(UserDto other) {
        this.id = other.id;
        this.username = other.username;
        this.email = other.email;
        this.userRole = other.userRole;
        this.displayName = other.displayName;
        this.avatarUrl = other.avatarUrl;
        this.bio = other.bio;
        this.isPublic = other.isPublic;
        this.createdAt = other.createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}