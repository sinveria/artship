package com.example.artship.features.comment.dto;

import java.time.LocalDateTime;

import com.example.artship.entities.comment.model.Comment;

public class CommentDto {

    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private Long artId;
    private Long userId;
    private Long parentCommentId;
    private boolean hidden;

    public CommentDto() {
    }

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.artId = comment.getArt() != null
                ? comment.getArt().getId()
                : null;
        this.userId = comment.getUser() != null
                ? comment.getUser().getId()
                : null;
        this.parentCommentId = comment.getParentComment() != null
                ? comment.getParentComment().getId()
                : null;
        this.hidden = comment.isHidden();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getArtId() {
        return artId;
    }

    public void setArtId(Long artId) {
        this.artId = artId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}