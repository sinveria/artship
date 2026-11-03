package com.example.artship.features.comment.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.artship.entities.comment.model.Comment;
import com.example.artship.features.comment.dto.CommentDto;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(comment);
    }

    public List<CommentDto> toDtos(List<Comment> comments) {
        if (comments == null) {
            return List.of();
        }

        return comments.stream()
                .map(this::toDto)
                .toList();
    }
}