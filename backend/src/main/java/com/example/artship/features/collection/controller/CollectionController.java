package com.example.artship.features.collection.controller;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;
import com.example.artship.features.collection.dto.CollectionDto;
import com.example.artship.features.collection.service.CollectionService;
import com.example.artship.features.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
@Tag(
        name = "Collection Controller",
        description = "API для управления коллекциями"
)
public class CollectionController {

    private final CollectionService collectionService;
    private final UserService userService;

    public CollectionController(
            CollectionService collectionService,
            UserService userService
    ) {
        this.collectionService = collectionService;
        this.userService = userService;
    }

    @Operation(summary = "Создать новую коллекцию")
    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String coverImageUrl,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOptional =
                userService.findByUsername(principal.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            User user = userOptional.get();

            CollectionDto collection = collectionService.createCollection(
                    title.trim(),
                    description != null ? description.trim() : null,
                    isPublic,
                    coverImageUrl,
                    user.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(collection);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Обновить коллекцию")
    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String coverImageUrl,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOptional =
                userService.findByUsername(principal.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<CollectionDto> collectionOptional =
                collectionService.getCollectionDtoById(id);

        if (collectionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userOptional.get();
        CollectionDto existingCollection = collectionOptional.get();

        if (!canManageCollection(currentUser, existingCollection)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CollectionDto updatedCollection =
                    collectionService.updateCollection(
                            id,
                            title,
                            description,
                            isPublic,
                            coverImageUrl
                    );

            return ResponseEntity.ok(updatedCollection);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получить коллекцию по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable Long id,
            Principal principal
    ) {
        Optional<CollectionDto> collectionOptional =
                collectionService.getCollectionDtoById(id);

        if (collectionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CollectionDto collection = collectionOptional.get();

        if (Boolean.TRUE.equals(collection.getIsPublic())) {
            return ResponseEntity.ok(collection);
        }

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<User> userOptional =
                userService.findByUsername(principal.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User currentUser = userOptional.get();

        if (!canManageCollection(currentUser, collection)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Получить все доступные коллекции пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CollectionDto>> getUserCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal
    ) {
        User currentUser = getCurrentUser(principal);

        if (currentUser != null
                && (isAdminOrModerator(currentUser)
                || currentUser.getId().equals(userId))) {

            return ResponseEntity.ok(
                    collectionService.getCollectionsByUserId(
                            userId,
                            pageable
                    )
            );
        }

        return ResponseEntity.ok(
                collectionService.getPublicCollectionsByUserId(
                        userId,
                        pageable
                )
        );
    }

    @Operation(summary = "Получить публичные коллекции пользователя")
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<Page<CollectionDto>> getUserPublicCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                collectionService.getPublicCollectionsByUserId(
                        userId,
                        pageable
                )
        );
    }

    @Operation(summary = "Получить все публичные коллекции")
    @GetMapping("/public")
    public ResponseEntity<Page<CollectionDto>> getPublicCollections(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                collectionService.getPublicCollections(pageable)
        );
    }

    @Operation(summary = "Поиск публичных коллекций")
    @GetMapping("/search")
    public ResponseEntity<Page<CollectionDto>> searchCollections(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                collectionService.searchPublicCollections(
                        q.trim(),
                        pageable
                )
        );
    }

    @Operation(summary = "Удалить коллекцию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long id,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOptional =
                userService.findByUsername(principal.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<CollectionDto> collectionOptional =
                collectionService.getCollectionDtoById(id);

        if (collectionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userOptional.get();
        CollectionDto collection = collectionOptional.get();

        if (!canManageCollection(currentUser, collection)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            collectionService.deleteCollection(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService
                .findByUsername(principal.getName())
                .orElse(null);
    }

    private boolean canManageCollection(
            User user,
            CollectionDto collection
    ) {
        return isAdminOrModerator(user)
                || (collection.getUserId() != null
                && collection.getUserId().equals(user.getId()));
    }

    private boolean isAdminOrModerator(User user) {
        return user.getUserRole() == UserRole.ADMIN
                || user.getUserRole() == UserRole.MODERATOR;
    }
}
