package com.example.artship.features.tag.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.entities.tag.model.Tag;
import com.example.artship.features.tag.dto.TagDto;
import com.example.artship.features.tag.service.TagService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/tags")
@Validated
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTagDtos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(
            @PathVariable Long id
    ) {
        return tagService.getTagDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagDto> getTagByName(
            @PathVariable String name
    ) {
        return tagService.getTagDtoByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagDto>> searchTags(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                tagService.searchTagDtos(name)
        );
    }

    @GetMapping("/art/{artId}")
    public ResponseEntity<List<TagDto>> getTagsByArtId(
            @PathVariable Long artId
    ) {
        return ResponseEntity.ok(
                tagService.getTagDtosByArtId(artId)
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TagDto>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                tagService.getPopularTagDtos(limit)
        );
    }

    @PostMapping
    public ResponseEntity<TagDto> createTag(
            @Valid @RequestBody TagRequest request
    ) {
        Tag tag = tagService.createTag(request.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TagDto(tag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request
    ) {
        Tag tag = tagService.updateTag(id, request.name());

        return ResponseEntity.ok(new TagDto(tag));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id
    ) {
        tagService.deleteTag(id);

        return ResponseEntity.noContent().build();
    }

    public record TagRequest(
            @NotBlank(message = "Название тега не должно быть пустым")
            @Size(max = 50, message = "Название тега не должно превышать 50 символов")
            String name
    ) {
    }
}