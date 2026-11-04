package com.example.artship.features.collection.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.model.CollectionArt;
import com.example.artship.entities.collection.repository.CollectionArtRepository;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.dto.CollectionDto;
import com.example.artship.features.collection.mapper.CollectionMapper;

@Service
@Transactional
public class CollectionService {

    private static final Logger log =
            LoggerFactory.getLogger(CollectionService.class);

    private static final String LIKED_COLLECTION_NAME = "Понравившиеся";

    private final CollectionRepository collectionRepository;
    private final CollectionArtRepository collectionArtRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;

    public CollectionService(
            CollectionRepository collectionRepository,
            CollectionArtRepository collectionArtRepository,
            UserRepository userRepository,
            CollectionMapper collectionMapper
    ) {
        this.collectionRepository = collectionRepository;
        this.collectionArtRepository = collectionArtRepository;
        this.userRepository = userRepository;
        this.collectionMapper = collectionMapper;
    }

    public CollectionDto createCollection(
            String title,
            String description,
            Boolean isPublic,
            String coverImageUrl,
            Long userId
    ) {
        log.info(
                "Creating collection: title='{}', userId={}",
                title,
                userId
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Пользователь не найден: " + userId
                        )
                );

        if (collectionRepository.existsByUserIdAndTitle(userId, title)) {
            throw new IllegalArgumentException(
                    "Коллекция с названием '" + title
                            + "' уже существует у пользователя"
            );
        }

        Collection collection = new Collection(title, user);
        collection.setDescription(description);
        collection.setIsPublic(isPublic != null ? isPublic : true);
        collection.setCoverImageUrl(coverImageUrl);

        Collection savedCollection = collectionRepository.save(collection);

        log.info(
                "Collection created with ID: {}",
                savedCollection.getId()
        );

        return convertToDtoWithArts(savedCollection);
    }

    @Transactional(readOnly = true)
    public Optional<Collection> getCollectionById(Long id) {
        return collectionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CollectionDto> getCollectionDtoById(Long id) {
        return collectionRepository.findById(id)
                .map(this::convertToDtoWithoutArts);
    }

    @Transactional(readOnly = true)
    public Optional<CollectionDto> getCollectionDtoWithArtsById(Long id) {
        return collectionRepository.findById(id)
                .map(this::convertToDtoWithArts);
    }

    public CollectionDto updateCollection(
            Long id,
            String title,
            String description,
            Boolean isPublic,
            String coverImageUrl
    ) {
        log.info("Updating collection {}", id);

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Коллекция не найдена: " + id
                        )
                );

        if (title != null && !title.equals(collection.getTitle())) {
            Long userId = collection.getUser().getId();

            if (collectionRepository.existsByUserIdAndTitle(userId, title)) {
                throw new IllegalArgumentException(
                        "Коллекция с таким названием уже существует"
                );
            }

            collection.setTitle(title);
        }

        if (description != null) {
            collection.setDescription(description);
        }

        if (isPublic != null) {
            collection.setIsPublic(isPublic);
        }

        if (coverImageUrl != null) {
            collection.setCoverImageUrl(coverImageUrl);
        }

        Collection updatedCollection =
                collectionRepository.save(collection);

        log.info("Collection {} updated successfully", id);

        return convertToDtoWithArts(updatedCollection);
    }

    public void deleteCollection(Long id) {
        log.info("Deleting collection with ID: {}", id);

        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Коллекция не найдена: " + id
                        )
                );

        if (LIKED_COLLECTION_NAME.equals(collection.getTitle())) {
            throw new IllegalArgumentException(
                    "Нельзя удалить системную коллекцию «Понравившиеся»"
            );
        }

        collectionArtRepository.deleteByCollectionId(id);
        collectionRepository.delete(collection);

        log.info("Collection {} deleted successfully", id);
    }

    @Transactional(readOnly = true)
    public Page<CollectionDto> getCollectionsByUserId(
            Long userId,
            Pageable pageable
    ) {
        Page<Collection> collectionsPage =
                collectionRepository.findByUserIdOrderByCreatedAtDesc(
                        userId,
                        pageable
                );

        return convertPageWithoutArts(collectionsPage, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CollectionDto> getPublicCollectionsByUserId(
            Long userId,
            Pageable pageable
    ) {
        Page<Collection> collectionsPage =
                collectionRepository
                        .findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(
                                userId,
                                pageable
                        );

        return convertPageWithoutArts(collectionsPage, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CollectionDto> getPublicCollections(
            Pageable pageable
    ) {
        Page<Collection> collectionsPage =
                collectionRepository.findByIsPublicTrueOrderByCreatedAtDesc(
                        pageable
                );

        return convertPageWithoutArts(collectionsPage, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CollectionDto> searchPublicCollections(
            String query,
            Pageable pageable
    ) {
        Page<Collection> collectionsPage =
                collectionRepository.searchPublicCollections(
                        query,
                        pageable
                );

        return convertPageWithoutArts(collectionsPage, pageable);
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsByUserId(Long userId) {
        return collectionMapper.toDtos(
                collectionRepository.findByUserId(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollectionsByUserId(Long userId) {
        return collectionRepository
                .findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDtoWithoutArts)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollections() {
        return collectionRepository
                .findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDtoWithoutArts)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> searchPublicCollections(String query) {
        return collectionRepository.searchPublicCollections(query)
                .stream()
                .map(this::convertToDtoWithoutArts)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsWithArtsByUserId(Long userId) {
        return collectionRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDtoWithArts)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isUserOwnerOfCollection(
            Long collectionId,
            Long userId
    ) {
        return collectionRepository.findById(collectionId)
                .map(collection ->
                        collection.getUser() != null
                                && collection.getUser().getId().equals(userId)
                )
                .orElse(false);
    }

    public void deleteAllUserCollections(Long userId) {
        List<Collection> userCollections =
                collectionRepository.findByUserId(userId);

        if (userCollections.isEmpty()) {
            log.info("No collections found for user ID: {}", userId);
            return;
        }

        collectionRepository.deleteByUserId(userId);

        log.info(
                "Successfully deleted {} collections for user ID: {}",
                userCollections.size(),
                userId
        );
    }

    private Page<CollectionDto> convertPageWithoutArts(
            Page<Collection> collectionsPage,
            Pageable pageable
    ) {
        List<CollectionDto> dtos = collectionsPage.getContent()
                .stream()
                .map(this::convertToDtoWithoutArts)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                collectionsPage.getTotalElements()
        );
    }

    private CollectionDto convertToDtoWithoutArts(
            Collection collection
    ) {
        CollectionDto dto = collectionMapper.toDto(collection);

        Long artCount = collectionArtRepository
                .countByCollectionIdAndArtStatus(
                        collection.getId(),
                        ArtStatus.ACTIVE
                );

        dto.setArtCount(artCount != null ? artCount.intValue() : 0);
        dto.setArts(Collections.emptyList());

        return dto;
    }

    private CollectionDto convertToDtoWithArts(
            Collection collection
    ) {
        CollectionDto dto = collectionMapper.toDto(collection);

        List<ArtDto> arts = collection.getCollectionArts()
                .stream()
                .filter(collectionArt ->
                        collectionArt.getArt() != null
                                && collectionArt.getArt().getStatus()
                                == ArtStatus.ACTIVE
                )
                .map(CollectionArt::getArt)
                .map(ArtDto::new)
                .toList();

        dto.setArtCount(arts.size());
        dto.setArts(arts);

        return dto;
    }
}