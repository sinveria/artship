package com.example.artship.features.like.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.service.CollectionArtService;

@Service
public class LikedArtService {

    private static final Logger log
            = LoggerFactory.getLogger(LikedArtService.class);

    private static final String LIKED_COLLECTION_NAME = "Понравившиеся";

    private final CollectionArtService collectionArtService;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    public LikedArtService(
            CollectionArtService collectionArtService,
            CollectionRepository collectionRepository,
            UserRepository userRepository
    ) {
        this.collectionArtService = collectionArtService;
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
    }

    private Collection getOrCreateLikedCollection(Long userId) {

        return collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(()
                                    -> new IllegalArgumentException(
                                            "User not found: " + userId
                                    )
                            );

                    log.info(
                            "Creating liked collection for user: {}",
                            userId
                    );

                    return collectionRepository.save(
                            new Collection(LIKED_COLLECTION_NAME, user)
                    );
                });
    }

    @Transactional
    public void addArtToLiked(Long userId, Long artId) {

        log.info("Adding art {} to liked collection of user {}", artId, userId);

        Collection collection = getOrCreateLikedCollection(userId);

        if (collectionArtService.existsByCollectionIdAndArtId(
                collection.getId(),
                artId
        )) {
            throw new IllegalArgumentException(
                    "Art already exists in liked collection"
            );
        }

        collectionArtService.addArtToCollection(
                collection.getId(),
                artId
        );
    }

    @Transactional
    public void removeArtFromLiked(Long userId, Long artId) {

        log.info(
                "Removing art {} from liked collection of user {}",
                artId,
                userId
        );

        Collection collection = collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "Liked collection not found"
                        )
                );

        if (!collectionArtService.existsByCollectionIdAndArtId(
                collection.getId(),
                artId
        )) {
            throw new IllegalArgumentException(
                    "Art not found in liked collection"
            );
        }

        collectionArtService.removeArtFromCollection(
                collection.getId(),
                artId
        );
    }

    @Transactional(readOnly = true)
    public boolean isArtLikedByUser(Long userId, Long artId) {

        return collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .map(collection
                        -> collectionArtService.existsByCollectionIdAndArtId(
                                collection.getId(),
                                artId
                        )
                )
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getLikedArtsByUser(
            Long userId,
            Pageable pageable
    ) {

        log.debug("Getting liked arts for user: {}", userId);

        return collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .map(collection
                        -> collectionArtService.getArtsByCollectionId(
                                collection.getId(),
                                pageable
                        )
                )
                .orElse(Page.empty(pageable));
    }

    @Transactional(readOnly = true)
    public long getLikedArtsCount(Long userId) {

        return collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .map(collection
                        -> collectionArtService.getArtCountByCollectionId(
                                collection.getId()
                        )
                )
                .orElse(0L);
    }

    @Transactional
    public void deleteAllLikedArtsByUser(Long userId) {

        log.info("Deleting liked collection for user: {}", userId);

        collectionRepository
                .findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
                .ifPresent(collection -> {
                    collectionArtService.removeAllArtsFromCollection(
                            collection.getId()
                    );

                    collectionRepository.delete(collection);
                });
    }
}
