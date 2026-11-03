package com.example.artship.features.art.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.art.dto.ArtDto;

@Service
@Transactional(readOnly = true)
public class ArtService {

    private final ArtRepository artRepository;
    private final UserRepository userRepository;

    public ArtService(
            ArtRepository artRepository,
            UserRepository userRepository
    ) {
        this.artRepository = artRepository;
        this.userRepository = userRepository;
    }

    public Art getArtById(Long id) {
        return artRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Арт с ID " + id + " не найден"
                        )
                );
    }

    public Page<Art> getAllArts(Pageable pageable) {
        return artRepository.findAll(pageable);
    }

    public Page<Art> getPublicArts(Pageable pageable) {
        return artRepository.findByIsPublicFlagTrueOrderByCreatedAtDesc(
                pageable
        );
    }

    public Page<Art> getArtsByAuthor(Long authorId, Pageable pageable) {
        User author = getUserById(authorId);

        return artRepository.findByAuthor(author, pageable);
    }

    public Page<Art> getPublicArtsByAuthor(
            Long authorId,
            Pageable pageable
    ) {
        User author = getUserById(authorId);

        return artRepository.findByAuthorAndIsPublicFlagTrue(
                author,
                pageable
        );
    }

    public Page<Art> searchByTitle(String title, Pageable pageable) {
        return artRepository
                .findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(
                        title,
                        pageable
                );
    }

    public Page<Art> getArtsByStatus(
            ArtStatus status,
            Pageable pageable
    ) {
        return artRepository.findByStatus(status, pageable);
    }

    public Page<Art> getPublicArtsByStatus(
            ArtStatus status,
            Pageable pageable
    ) {
        return artRepository.findByStatusAndIsPublicFlagTrue(
                status,
                pageable
        );
    }

    public Page<Art> getArtsByTag(Long tagId, Pageable pageable) {
        return artRepository.findByTagsId(tagId, pageable);
    }

    public Page<Art> getArtsByTagName(
            String tagName,
            Pageable pageable
    ) {
        return artRepository.findByTagNameAndIsPublicFlagTrue(
                tagName,
                pageable
        );
    }

    public long countByStatus(ArtStatus status) {
        return artRepository.countByStatus(status);
    }

    @Transactional
    public Art createArt(ArtDto artDto) {
        if (artDto == null) {
            throw new IllegalArgumentException(
                    "Данные арта не могут быть пустыми"
            );
        }

        if (artDto.getAuthor() == null
                || artDto.getAuthor().getId() == null) {
            throw new IllegalArgumentException(
                    "Необходимо указать автора арта"
            );
        }

        User author = getUserById(artDto.getAuthor().getId());

        Art art = new Art();
        art.setTitle(artDto.getTitle());
        art.setDescription(artDto.getDescription());
        art.setImageUrl(artDto.getImage());
        art.setProjectDataUrl(artDto.getProjectDataUrl());
        art.setIsPublicFlag(artDto.isPublicFlag());
        art.setStatus(
                artDto.getStatus() != null
                        ? artDto.getStatus()
                        : ArtStatus.ACTIVE
        );
        art.setAuthor(author);

        return artRepository.save(art);
    }

    @Transactional
    public Art updateArt(Long id, ArtDto artDto) {
        if (artDto == null) {
            throw new IllegalArgumentException(
                    "Данные арта не могут быть пустыми"
            );
        }

        Art existingArt = getArtById(id);

        if (artDto.getTitle() != null) {
            existingArt.setTitle(artDto.getTitle());
        }

        if (artDto.getDescription() != null) {
            existingArt.setDescription(artDto.getDescription());
        }

        if (artDto.getImage() != null) {
            existingArt.setImageUrl(artDto.getImage());
        }

        if (artDto.getProjectDataUrl() != null) {
            existingArt.setProjectDataUrl(
                    artDto.getProjectDataUrl()
            );
        }

        existingArt.setIsPublicFlag(artDto.isPublicFlag());

        if (artDto.getStatus() != null) {
            existingArt.setStatus(artDto.getStatus());
        }

        if (artDto.getAuthor() != null
                && artDto.getAuthor().getId() != null) {
            User author = getUserById(artDto.getAuthor().getId());
            existingArt.setAuthor(author);
        }

        return artRepository.save(existingArt);
    }

    @Transactional
    public void deleteArt(Long id) {
        Art art = getArtById(id);
        artRepository.delete(art);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Пользователь с ID " + id + " не найден"
                        )
                );
    }
}