package com.example.artship.features.collection.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.model.CollectionArt;
import com.example.artship.entities.collection.repository.CollectionArtRepository;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.dto.CollectionArtDto;
import com.example.artship.features.collection.mapper.CollectionArtMapper;

@Service
@Transactional
public class CollectionArtService {

    private static final Logger log =
            LoggerFactory.getLogger(CollectionArtService.class);

    private final CollectionArtRepository collectionArtRepository;
    private final CollectionRepository collectionRepository;
    private final ArtRepository artRepository;
    private final CollectionArtMapper collectionArtMapper;

    public CollectionArtService(
            CollectionArtRepository collectionArtRepository,
            CollectionRepository collectionRepository,
            ArtRepository artRepository,
            CollectionArtMapper collectionArtMapper
    ) {
        this.collectionArtRepository = collectionArtRepository;
        this.collectionRepository = collectionRepository;
        this.artRepository = artRepository;
        this.collectionArtMapper = collectionArtMapper;
    }

    public CollectionArtDto addArtToCollection(
            Long collectionId,
            Long artId
    ) {
        log.info(
                "Adding art {} to collection {}",
                artId,
                collectionId
        );

        Collection collection = findCollection(collectionId);
        Art art = findArt(artId);

        boolean isPublic = Boolean.TRUE.equals(art.getIsPublicFlag());

        boolean isAuthor = art.getAuthor() != null
                && art.getAuthor().getId() != null
                && art.getAuthor().getId().equals(
                        collection.getUser().getId()
                );

        if (!isPublic && !isAuthor) {
            throw new IllegalArgumentException(
                    "Нельзя добавить приватное произведение в эту коллекцию"
            );
        }

        if (art.getStatus() != ArtStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Нельзя добавить произведение со статусом: "
                            + art.getStatus()
            );
        }

        if (collectionArtRepository.existsByCollectionIdAndArtId(
                collectionId,
                artId
        )) {
            throw new IllegalArgumentException(
                    "Произведение уже добавлено в коллекцию"
            );
        }

        CollectionArt collectionArt =
                new CollectionArt(collection, art);

        CollectionArt savedCollectionArt =
                collectionArtRepository.save(collectionArt);

        if (collection.getCoverImageUrl() == null
                || collection.getCoverImageUrl().isEmpty()) {

            collection.setCoverImageUrl(art.getImageUrl());
            collectionRepository.save(collection);

            log.info(
                    "Set cover image for collection {}",
                    collectionId
            );
        }

        return collectionArtMapper.toDto(savedCollectionArt);
    }

    public void removeArtFromCollection(
            Long collectionId,
            Long artId
    ) {
        log.info(
                "Removing art {} from collection {}",
                artId,
                collectionId
        );

        if (!collectionArtRepository.existsByCollectionIdAndArtId(
                collectionId,
                artId
        )) {
            log.warn(
                    "Art {} not found in collection {}",
                    artId,
                    collectionId
            );
            return;
        }

        collectionArtRepository.deleteByCollectionIdAndArtId(
                collectionId,
                artId
        );

        updateCollectionCoverIfNeeded(collectionId);
    }

    public void removeAllArtsFromCollection(Long collectionId) {
        log.info(
                "Removing all arts from collection {}",
                collectionId
        );

        Collection collection = findCollection(collectionId);

        Long count = collectionArtRepository.countByCollectionId(
                collectionId
        );

        collectionArtRepository.deleteByCollectionId(collectionId);

        collection.setCoverImageUrl(null);
        collectionRepository.save(collection);

        log.info(
                "Removed {} arts from collection {}",
                count,
                collectionId
        );
    }

    private void updateCollectionCoverIfNeeded(Long collectionId) {
        Collection collection = findCollection(collectionId);

        if (collection.getCoverImageUrl() == null) {
            return;
        }

        List<CollectionArt> arts =
                collectionArtRepository.findByCollectionId(collectionId);

        if (arts.isEmpty()) {
            collection.setCoverImageUrl(null);
        } else {
            String newCoverUrl = arts.get(0).getArt().getImageUrl();

            if (!newCoverUrl.equals(collection.getCoverImageUrl())) {
                collection.setCoverImageUrl(newCoverUrl);
            }
        }

        collectionRepository.save(collection);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getArtsByCollectionId(
            Long collectionId,
            Pageable pageable
    ) {
        Page<CollectionArt> collectionArtsPage =
                collectionArtRepository.findByCollectionIdAndArtStatus(
                        collectionId,
                        ArtStatus.ACTIVE,
                        pageable
                );

        List<ArtDto> artDtos = collectionArtsPage.getContent()
                .stream()
                .map(CollectionArt::getArt)
                .filter(art -> art != null)
                .map(ArtDto::new)
                .toList();

        return new PageImpl<>(
                artDtos,
                pageable,
                collectionArtsPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<CollectionArtDto> getCollectionsByArtId(
            Long artId,
            Pageable pageable
    ) {
        Page<CollectionArt> page =
                collectionArtRepository.findByArtId(artId, pageable);

        return page.map(collectionArtMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CollectionArtDto> getCollectionArtDtosByCollectionId(
            Long collectionId,
            Pageable pageable
    ) {
        Page<CollectionArt> page =
                collectionArtRepository.findByCollectionId(
                        collectionId,
                        pageable
                );

        return page.map(collectionArtMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<ArtDto> getAllArtsByCollectionId(Long collectionId) {
        List<CollectionArt> collectionArts =
                collectionArtRepository.findByCollectionId(collectionId);

        if (collectionArts == null || collectionArts.isEmpty()) {
            return Collections.emptyList();
        }

        return collectionArts.stream()
                .map(CollectionArt::getArt)
                .filter(art ->
                        art != null
                                && art.getStatus() == ArtStatus.ACTIVE
                )
                .map(ArtDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionsByArtId(Long artId) {
        return collectionArtMapper.toDtos(
                collectionArtRepository.findByArtId(artId)
        );
    }

    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionArtDtosByCollectionId(
            Long collectionId
    ) {
        return collectionArtMapper.toDtos(
                collectionArtRepository.findByCollectionId(collectionId)
        );
    }

    @Transactional(readOnly = true)
    public Long getArtCountByCollectionId(Long collectionId) {
        return collectionArtRepository.countByCollectionIdAndArtStatus(
                collectionId,
                ArtStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public boolean existsByCollectionIdAndArtId(
            Long collectionId,
            Long artId
    ) {
        return collectionArtRepository.existsByCollectionIdAndArtId(
                collectionId,
                artId
        );
    }

    public void moveArtBetweenCollections(
            Long artId,
            Long fromCollectionId,
            Long toCollectionId
    ) {
        if (fromCollectionId.equals(toCollectionId)) {
            throw new IllegalArgumentException(
                    "Исходная и целевая коллекции совпадают"
            );
        }

        findCollection(fromCollectionId);
        findCollection(toCollectionId);

        if (!collectionArtRepository.existsByCollectionIdAndArtId(
                fromCollectionId,
                artId
        )) {
            throw new IllegalArgumentException(
                    "Произведение не найдено в исходной коллекции"
            );
        }

        if (collectionArtRepository.existsByCollectionIdAndArtId(
                toCollectionId,
                artId
        )) {
            throw new IllegalArgumentException(
                    "Произведение уже есть в целевой коллекции"
            );
        }

        collectionArtRepository.deleteByCollectionIdAndArtId(
                fromCollectionId,
                artId
        );

        updateCollectionCoverIfNeeded(fromCollectionId);

        addArtToCollection(toCollectionId, artId);
    }

    public CollectionArtDto copyArtToCollection(
            Long artId,
            Long collectionId
    ) {
        return addArtToCollection(collectionId, artId);
    }

    private Collection findCollection(Long collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Коллекция не найдена: " + collectionId
                        )
                );
    }

    private Art findArt(Long artId) {
        return artRepository.findById(artId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Произведение не найдено: " + artId
                        )
                );
    }
}