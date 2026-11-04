package com.example.artship.features.follow.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.follow.model.Follow;
import com.example.artship.entities.follow.repository.FollowRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.follow.dto.FollowDto;

@Service
@Transactional
public class FollowService {

    private static final Logger log =
            LoggerFactory.getLogger(FollowService.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(
            FollowRepository followRepository,
            UserRepository userRepository
    ) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public FollowDto followUser(Long followerId, Long followingId) {

        log.info(
                "Following user: followerId={}, followingId={}",
                followerId,
                followingId
        );

        if (followerId == null || followingId == null) {
            throw new IllegalArgumentException(
                    "User IDs must not be null"
            );
        }

        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException(
                    "Cannot follow yourself"
            );
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Follower user not found: " + followerId
                        )
                );

        User following = userRepository.findById(followingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Following user not found: " + followingId
                        )
                );

        if (followRepository.existsByFollowerIdAndFollowingId(
                followerId,
                followingId
        )) {
            throw new IllegalArgumentException(
                    "User is already following this user"
            );
        }

        Follow follow = new Follow(follower, following);

        Follow savedFollow = followRepository.save(follow);

        log.info(
                "Follow created successfully: {} -> {}",
                follower.getUsername(),
                following.getUsername()
        );

        return new FollowDto(savedFollow);
    }

    public void unfollowUser(Long followerId, Long followingId) {

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(
                        followerId,
                        followingId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Follow relationship not found"
                        )
                );

        followRepository.delete(follow);

        log.info(
                "Unfollowed successfully: {} -> {}",
                followerId,
                followingId
        );
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(
            Long followerId,
            Long followingId
    ) {
        return followRepository.existsByFollowerIdAndFollowingId(
                followerId,
                followingId
        );
    }

    @Transactional(readOnly = true)
    public Page<FollowDto> getFollowers(
            Long userId,
            Pageable pageable
    ) {
        Page<Follow> followersPage =
                followRepository.findByFollowingId(userId, pageable);

        List<FollowDto> dtos = followersPage.getContent()
                .stream()
                .map(FollowDto::new)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                followersPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<FollowDto> getFollowing(
            Long userId,
            Pageable pageable
    ) {
        Page<Follow> followingPage =
                followRepository.findByFollowerId(userId, pageable);

        List<FollowDto> dtos = followingPage.getContent()
                .stream()
                .map(FollowDto::new)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                followingPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public List<FollowDto> getFollowers(Long userId) {

        return followRepository.findByFollowingId(userId)
                .stream()
                .map(FollowDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowDto> getFollowing(Long userId) {

        return followRepository.findByFollowerId(userId)
                .stream()
                .map(FollowDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowersByUsername(
            Long userId,
            String usernameSearch,
            Pageable pageable
    ) {

        if (usernameSearch == null || usernameSearch.isBlank()) {
            return getFollowers(userId, pageable);
        }

        Page<Follow> followersPage =
                followRepository.searchFollowersByUsername(
                        userId,
                        usernameSearch.trim(),
                        pageable
                );

        List<FollowDto> dtos = followersPage.getContent()
                .stream()
                .map(FollowDto::new)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                followersPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowingByUsername(
            Long userId,
            String usernameSearch,
            Pageable pageable
    ) {

        if (usernameSearch == null || usernameSearch.isBlank()) {
            return getFollowing(userId, pageable);
        }

        Page<Follow> followingPage =
                followRepository.searchFollowingByUsername(
                        userId,
                        usernameSearch.trim(),
                        pageable
                );

        List<FollowDto> dtos = followingPage.getContent()
                .stream()
                .map(FollowDto::new)
                .toList();

        return new PageImpl<>(
                dtos,
                pageable,
                followingPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowersByUsername(
            Long userId,
            String usernameSearch,
            Pageable pageable,
            boolean excludeCurrentUser
    ) {

        Page<FollowDto> result = searchFollowersByUsername(
                userId,
                usernameSearch,
                pageable
        );

        if (!excludeCurrentUser) {
            return result;
        }

        List<FollowDto> filtered = result.getContent()
                .stream()
                .filter(follow ->
                        !follow.getFollower().getId().equals(userId)
                )
                .toList();

        return new PageImpl<>(
                filtered,
                pageable,
                filtered.size()
        );
    }

    @Transactional(readOnly = true)
    public Long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Transactional(readOnly = true)
    public Long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Transactional(readOnly = true)
    public Long countFollowersBySearch(
            Long userId,
            String usernameSearch
    ) {

        if (usernameSearch == null || usernameSearch.isBlank()) {
            return getFollowerCount(userId);
        }

        return followRepository.countFollowersByUsername(
                userId,
                usernameSearch.trim()
        );
    }

    @Transactional(readOnly = true)
    public Long countFollowingBySearch(
            Long userId,
            String usernameSearch
    ) {

        if (usernameSearch == null || usernameSearch.isBlank()) {
            return getFollowingCount(userId);
        }

        return followRepository.countFollowingByUsername(
                userId,
                usernameSearch.trim()
        );
    }

    public void deleteAllUserFollows(Long userId) {

        followRepository.deleteAllByFollowerId(userId);
        followRepository.deleteAllByFollowingId(userId);

        log.info(
                "All follow relationships deleted for user: {}",
                userId
        );
    }
}