package com.example.artship.features.collection.controller;

import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.dto.CollectionArtDto;
import com.example.artship.features.collection.dto.CopyArtRequest;
import com.example.artship.features.collection.dto.MoveArtRequest;
import com.example.artship.features.collection.service.CollectionArtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/collection-arts")
@Tag(
        name = "CollectionArt Controller",
        description = "API для управления артами в коллекциях"
)
public class CollectionArtController {

    private static final Logger log =
            LoggerFactory.getLogger(CollectionArtController.class);

    private final CollectionArtService collectionArtService;

    public CollectionArtController(CollectionArtService collectionArtService) {
        this.collectionArtService = collectionArtService;
    }

    @Operation(summary = "Добавить арт в коллекцию")
    @PostMapping("/collection/{collectionId}/art/{artId}")
    public ResponseEntity<?> addArtToCollection(
            @PathVariable Long collectionId,
            @PathVariable Long artId) {

        log.info(
                "Добавление арта {} в коллекцию {}",
                artId,
                collectionId
        );

        try {
            CollectionArtDto collectionArt =
                    collectionArtService.addArtToCollection(
                            collectionId,
                            artId
                    );

            URI location = URI.create(
                    "/api/collection-arts/collection/"
                            + collectionId
                            + "/art/"
                            + artId
            );

            return ResponseEntity.created(location).body(collectionArt);

        } catch (RuntimeException e) {
            log.error(
                    "Ошибка добавления арта {} в коллекцию {}: {}",
                    artId,
                    collectionId,
                    e.getMessage()
            );

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "Удалить арт из коллекции")
    @DeleteMapping("/collection/{collectionId}/art/{artId}")
    public ResponseEntity<Void> removeArtFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long artId) {

        try {
            collectionArtService.removeArtFromCollection(
                    collectionId,
                    artId
            );

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.error(
                    "Ошибка удаления арта {} из коллекции {}: {}",
                    artId,
                    collectionId,
                    e.getMessage()
            );

            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Получить арты коллекции")
    @GetMapping("/collection/{collectionId}/arts")
    public ResponseEntity<Page<ArtDto>> getArtsByCollection(
            @PathVariable Long collectionId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ArtDto> arts =
                collectionArtService.getArtsByCollectionId(
                        collectionId,
                        pageable
                );

        return ResponseEntity.ok(arts);
    }

    @Operation(summary = "Получить коллекции, содержащие арт")
    @GetMapping("/art/{artId}/collections")
    public ResponseEntity<Page<CollectionArtDto>> getCollectionsByArt(
            @PathVariable Long artId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CollectionArtDto> collections =
                collectionArtService.getCollectionsByArtId(
                        artId,
                        pageable
                );

        return ResponseEntity.ok(collections);
    }

    @Operation(summary = "Получить связи артов с коллекцией")
    @GetMapping("/collection/{collectionId}/collection-arts")
    public ResponseEntity<Page<CollectionArtDto>> getCollectionArts(
            @PathVariable Long collectionId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<CollectionArtDto> collectionArts =
                collectionArtService.getCollectionArtDtosByCollectionId(
                        collectionId,
                        pageable
                );

        return ResponseEntity.ok(collectionArts);
    }

    @Operation(summary = "Получить количество артов в коллекции")
    @GetMapping("/collection/{collectionId}/arts/count")
    public ResponseEntity<Long> getArtCountByCollection(
            @PathVariable Long collectionId) {

        Long count =
                collectionArtService.getArtCountByCollectionId(collectionId);

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Проверить наличие арта в коллекции")
    @GetMapping("/collection/{collectionId}/art/{artId}/exists")
    public ResponseEntity<Boolean> checkArtInCollection(
            @PathVariable Long collectionId,
            @PathVariable Long artId) {

        boolean exists =
                collectionArtService.existsByCollectionIdAndArtId(
                        collectionId,
                        artId
                );

        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Переместить арт между коллекциями")
    @PostMapping("/art/{artId}/move")
    public ResponseEntity<Void> moveArtBetweenCollections(
            @PathVariable Long artId,
            @RequestBody MoveArtRequest request) {

        try {
            collectionArtService.moveArtBetweenCollections(
                    artId,
                    request.getFromCollectionId(),
                    request.getToCollectionId()
            );

            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            log.error(
                    "Ошибка перемещения арта {}: {}",
                    artId,
                    e.getMessage()
            );

            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Скопировать арт в коллекцию")
    @PostMapping("/art/{artId}/copy")
    public ResponseEntity<CollectionArtDto> copyArtToCollection(
            @PathVariable Long artId,
            @RequestBody CopyArtRequest request) {

        try {
            CollectionArtDto collectionArt =
                    collectionArtService.copyArtToCollection(
                            artId,
                            request.getCollectionId()
                    );

            return ResponseEntity.ok(collectionArt);

        } catch (RuntimeException e) {
            log.error(
                    "Ошибка копирования арта {}: {}",
                    artId,
                    e.getMessage()
            );

            return ResponseEntity.badRequest().build();
        }
    }
}