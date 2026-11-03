package com.example.artship.features.art.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.art.mapper.ArtMapper;
import com.example.artship.features.art.service.ArtService;

@RestController
@RequestMapping("/api/arts")
public class ArtController {

    private final ArtService artService;
    private final ArtMapper artMapper;

    public ArtController(
            ArtService artService,
            ArtMapper artMapper
    ) {
        this.artService = artService;
        this.artMapper = artMapper;
    }

    @GetMapping
    public ResponseEntity<Page<ArtDto>> getAllArts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .getPublicArts(pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtDto> getArtById(
            @PathVariable Long id
    ) {
        Art art = artService.getArtById(id);

        return ResponseEntity.ok(artMapper.toDto(art));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<ArtDto>> getArtsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .getPublicArtsByAuthor(authorId, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArtDto>> searchArts(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .searchByTitle(title, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ArtDto>> getArtsByStatus(
            @PathVariable ArtStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .getPublicArtsByStatus(status, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Page<ArtDto>> getArtsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .getArtsByTag(tagId, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tag/name/{tagName}")
    public ResponseEntity<Page<ArtDto>> getArtsByTagName(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<ArtDto> result = artService
                .getArtsByTagName(tagName, pageable)
                .map(artMapper::toDto);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ArtDto> createArt(
            @RequestBody ArtDto artDto
    ) {
        Art createdArt = artService.createArt(artDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(artMapper.toDto(createdArt));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtDto> updateArt(
            @PathVariable Long id,
            @RequestBody ArtDto artDto
    ) {
        Art updatedArt = artService.updateArt(id, artDto);

        return ResponseEntity.ok(artMapper.toDto(updatedArt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArt(
            @PathVariable Long id
    ) {
        artService.deleteArt(id);

        return ResponseEntity.noContent().build();
    }
}