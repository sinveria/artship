package com.example.artship.features.user.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.follow.service.FollowService;
import com.example.artship.features.user.dto.RoleStatistics;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FollowService followService;

    public UserService(
            UserRepository userRepository,
            FollowService followService
    ) {
        this.userRepository = userRepository;
        this.followService = followService;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя вместе со всеми его подписками
     * и подписчиками.
     */
    @Transactional
    public void deleteById(Long id) {
        followService.deleteAllUserFollows(id);
        userRepository.deleteById(id);

        logger.info(
                "User and all follow relationships deleted: userId={}",
                id
        );
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByRole(
            UserRole role,
            Pageable pageable
    ) {
        return userRepository.findByUserRole(role, pageable);
    }

    @Transactional
    public User changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );

        user.setUserRole(newRole);

        return userRepository.save(user);
    }

    @Transactional
    public User changeUserRoleByUsername(
            String username,
            UserRole newRole
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with username: " + username
                        )
                );

        user.setUserRole(newRole);

        return userRepository.save(user);
    }

    @Transactional
    public int bulkChangeUserRole(
            List<Long> userIds,
            UserRole newRole
    ) {
        int updatedCount = 0;

        for (Long userId : userIds) {
            try {
                changeUserRole(userId, newRole);
                updatedCount++;
            } catch (IllegalArgumentException e) {
                logger.warn(
                        "User with ID {} was not found and skipped",
                        userId
                );
            }
        }

        logger.info("Successfully updated {} users", updatedCount);

        return updatedCount;
    }

    @Transactional(readOnly = true)
    public RoleStatistics getRoleStatistics() {
        logger.info("Getting role statistics");

        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByUserRole(UserRole.ADMIN);
        long moderatorCount =
                userRepository.countByUserRole(UserRole.MODERATOR);
        long userCount = userRepository.countByUserRole(UserRole.USER);

        return new RoleStatistics(
                totalUsers,
                adminCount,
                moderatorCount,
                userCount
        );
    }

    @Transactional(readOnly = true)
    public Page<User> findPublicUsers(Pageable pageable) {
        logger.info(
                "Finding public users: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return userRepository.findByIsPublicTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        logger.info(
                "Getting all users: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchByUsername(
            String search,
            Pageable pageable
    ) {
        logger.info("Searching users by username: {}", search);

        return userRepository
                .findByUsernameContainingIgnoreCase(search, pageable);
    }

    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, UserRole role) {
        return userRepository.findById(userId)
                .map(user -> user.getUserRole() == role)
                .orElse(false);
    }
}