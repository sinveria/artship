package com.example.artship.features.comment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.features.comment.dto.CommentDto;
import com.example.artship.features.comment.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentDto comment = commentService.createComment(
                request.text(),
                request.artId(),
                request.userId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comment);
    }

    @PostMapping("/reply")
    public ResponseEntity<CommentDto> createReply(
            @Valid @RequestBody CreateReplyRequest request
    ) {
        CommentDto reply = commentService.createReply(
                request.text(),
                request.parentCommentId(),
                request.userId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reply);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getCommentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                commentService.getCommentById(id)
        );
    }

    @GetMapping("/art/{artId}")
    public ResponseEntity<List<CommentDto>> getCommentsByArtId(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                commentService.getCommentsByArtId(artId)
        );
    }

    @GetMapping("/art/{artId}/root")
    public ResponseEntity<List<CommentDto>> getRootCommentsByArtId(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                commentService.getRootCommentsByArtId(artId)
        );
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                commentService.getReplies(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                commentService.getCommentsByUserId(userId)
        );
    }

    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> countCommentsByArtId(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                commentService.countCommentsByArtId(artId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(
                        id,
                        request.userId(),
                        request.text()
                )
        );
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<CommentDto> hideComment(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                commentService.hideComment(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        commentService.deleteComment(id, userId);

        return ResponseEntity.noContent().build();
    }

    public record CreateCommentRequest(
            @NotBlank
            @Size(max = 2000)
            String text,

            Long artId,
            Long userId
    ) {
    }

    public record CreateReplyRequest(
            @NotBlank
            @Size(max = 2000)
            String text,

            Long parentCommentId,
            Long userId
    ) {
    }

    public record UpdateCommentRequest(
            @NotBlank
            @Size(max = 2000)
            String text,

            Long userId
    ) {
    }
}