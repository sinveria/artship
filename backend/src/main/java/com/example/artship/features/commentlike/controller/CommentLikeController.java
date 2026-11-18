package com.example.artship.features.commentlike.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.features.commentlike.service.CommentLikeService;

@RestController
@RequestMapping("/api/comments/{commentId}/likes")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PostMapping
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentLikeService.likeComment(userId, commentId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentLikeService.unlikeComment(userId, commentId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> isLikedByUser(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                commentLikeService.isLikedByUser(userId, commentId)
        );
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getLikesCount(
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(
                commentLikeService.getLikesCount(commentId)
        );
    }
}
