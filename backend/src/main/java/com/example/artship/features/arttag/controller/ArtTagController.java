package com.example.artship.features.arttag.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.art.mapper.ArtMapper;
import com.example.artship.features.art.service.ArtService;
import com.example.artship.features.arttag.dto.ArtTagDto;
import com.example.artship.features.arttag.service.ArtTagService;
import com.example.artship.features.tag.dto.TagDto;
import com.example.artship.features.tag.service.TagService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/art-tags")
public class ArtTagController {

    private final ArtTagService artTagService;
    private final TagService tagService;
    private final ArtService artService;
    private final ArtMapper artMapper;

    public ArtTagController(
            ArtTagService artTagService,
            TagService tagService,
            ArtService artService,
            ArtMapper artMapper
    ) {
        this.artTagService = artTagService;
        this.tagService = tagService;
        this.artService = artService;
        this.artMapper = artMapper;
    }

    @PostMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<?> addTagToArt(
            @PathVariable Long artId,
            @PathVariable Long tagId
    ) {
        try {
            ArtTagDto result
                    = artTagService.createArtTag(artId, tagId);

            URI location = URI.create(
                    "/api/art-tags/art/" + artId + "/tag/" + tagId
            );

            return ResponseEntity
                    .created(location)
                    .body(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<?> removeTagFromArt(
            @PathVariable Long artId,
            @PathVariable Long tagId
    ) {
        try {
            artTagService.removeTagFromArt(artId, tagId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/art/{artId}/tag/{tagId}/exists")
    public ResponseEntity<Boolean> checkTagArtRelation(
            @PathVariable Long artId,
            @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(
                artTagService.existsByArtIdAndTagId(artId, tagId)
        );
    }

    @GetMapping("/art/{artId}/art-tags")
    public ResponseEntity<Page<ArtTagDto>> getArtTagsByArt(
            @PathVariable Long artId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                artTagService.getArtTagDtosByArtId(artId, pageable)
        );
    }

    @GetMapping("/tag/{tagId}/art-tags")
    public ResponseEntity<Page<ArtTagDto>> getArtTagsByTag(
            @PathVariable Long tagId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                artTagService.getArtTagDtosByTagId(tagId, pageable)
        );
    }

    @GetMapping("/tag/{tagId}/arts/count")
    public ResponseEntity<Long> getArtCountByTag(
            @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(
                artTagService.getArtCountByTagId(tagId)
        );
    }

    @GetMapping("/art/{artId}/tags/count")
    public ResponseEntity<Long> getTagCountByArt(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                artTagService.getTagCountByArtId(artId)
        );
    }

    @DeleteMapping("/art/{artId}/tags")
    public ResponseEntity<?> removeAllTagsFromArt(
            @PathVariable Long artId
    ) {
        try {
            artTagService.removeAllTagsFromArt(artId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/art/{artId}/tags/batch")
    public ResponseEntity<?> addTagsBatchToArt(
            @PathVariable Long artId,
            @Valid @RequestBody AddTagsRequest request
    ) {
        try {
            artTagService.addTagsToArt(artId, request.tagNames());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<ArtTagDto> getArtTagRelation(
            @PathVariable Long artId,
            @PathVariable Long tagId
    ) {
        return artTagService
                .getArtTagDtoByArtIdAndTagId(artId, tagId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/art/{artId}/tags")
    public ResponseEntity<List<TagDto>> getTagsByArt(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                tagService.getTagDtosByArtId(artId)
        );
    }

    @GetMapping("/tag/{tagId}/arts")
    public ResponseEntity<Page<ArtDto>> getArtsByTag(
            @PathVariable Long tagId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ArtDto> arts = artService
                .getArtsByTag(tagId, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(arts);
    }

    public record AddTagsRequest(
            @NotNull(message = "Список тегов не должен быть null")
            @NotEmpty(message = "Необходимо передать хотя бы один тег")
            List<String> tagNames
            ) {

    }
}
