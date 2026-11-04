package com.example.artship.features.like.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.features.like.dto.LikeDto;
import com.example.artship.features.like.service.LikeService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/likes")
@Tag(name = "Like Controller", description = "API для управления лайками работ")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/user/{userId}/art/{artId}")
    public ResponseEntity<?> addLike(
            @PathVariable Long userId,
            @PathVariable Long artId) {

        try {
            LikeDto like = likeService.addLike(userId, artId);
            return ResponseEntity.ok(like);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/user/{userId}/art/{artId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long userId,
            @PathVariable Long artId) {

        try {
            likeService.removeLike(userId, artId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}/art/{artId}/exists")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long userId,
            @PathVariable Long artId) {

        return ResponseEntity.ok(likeService.isLiked(userId, artId));
    }

    @GetMapping("/art/{artId}")
    public ResponseEntity<Page<LikeDto>> getLikesByArt(
            @PathVariable Long artId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                likeService.getLikesByArtId(artId, pageable)
        );
    }

    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> getLikeCountByArt(
            @PathVariable Long artId) {

        return ResponseEntity.ok(
                likeService.getLikeCountByArtId(artId)
        );
    }
}
