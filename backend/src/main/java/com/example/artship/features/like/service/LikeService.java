package com.example.artship.features.like.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.like.model.ArtLikes;
import com.example.artship.entities.like.repository.LikeRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.like.dto.LikeDto;

@Service
@Transactional
public class LikeService {

    private static final Logger log
            = LoggerFactory.getLogger(LikeService.class);

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ArtRepository artRepository;
    private final LikedArtService likedArtService;

    public LikeService(
            LikeRepository likeRepository,
            UserRepository userRepository,
            ArtRepository artRepository,
            LikedArtService likedArtService
    ) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.artRepository = artRepository;
        this.likedArtService = likedArtService;
    }

    public LikeDto addLike(Long userId, Long artId) {

        log.info("User {} likes art {}", userId, artId);

        User user = userRepository.findById(userId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        Art art = artRepository.findById(artId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "Art not found: " + artId
                        )
                );

        if (likeRepository.existsByUserIdAndArtId(userId, artId)) {
            throw new IllegalArgumentException(
                    "Like already exists"
            );
        }

        ArtLikes like = new ArtLikes(user, art);

        ArtLikes savedLike = likeRepository.save(like);

        try {
            likedArtService.addArtToLiked(userId, artId);

            log.info(
                    "Art {} added to liked collection of user {}",
                    artId,
                    userId
            );
        } catch (RuntimeException e) {
            log.warn(
                    "Could not add art to liked collection: {}",
                    e.getMessage()
            );
        }

        return new LikeDto(savedLike);
    }

    public void removeLike(Long userId, Long artId) {

        log.info("User {} unlikes art {}", userId, artId);

        if (!likeRepository.existsByUserIdAndArtId(userId, artId)) {
            throw new IllegalArgumentException(
                    "Like does not exist"
            );
        }

        likeRepository.deleteByUserIdAndArtId(userId, artId);

        try {
            likedArtService.removeArtFromLiked(userId, artId);

            log.info(
                    "Art {} removed from liked collection of user {}",
                    artId,
                    userId
            );
        } catch (RuntimeException e) {
            log.warn(
                    "Could not remove art from liked collection: {}",
                    e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long artId) {

        return likeRepository.existsByUserIdAndArtId(
                userId,
                artId
        );
    }

    @Transactional(readOnly = true)
    public Page<LikeDto> getLikesByArtId(
            Long artId,
            Pageable pageable
    ) {

        Page<ArtLikes> likesPage
                = likeRepository.findByArtId(artId, pageable);

        List<LikeDto> dtos = likesPage.getContent()
                .stream()
                .map(LikeDto::new)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                likesPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Long getLikeCountByArtId(Long artId) {

        return likeRepository.countByArtId(artId);
    }

    @Transactional(readOnly = true)
    public Long getLikeCountByUserId(Long userId) {

        return likeRepository.countByUserId(userId);
    }

    public void deleteAllUserLikes(Long userId) {

        log.info("Deleting all likes for user: {}", userId);

        likeRepository.deleteByUserId(userId);

        likedArtService.deleteAllLikedArtsByUser(userId);
    }
}
