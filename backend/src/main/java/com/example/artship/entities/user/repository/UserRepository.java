package com.example.artship.entities.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Page<User> findByUsernameContainingIgnoreCase(
            String username,
            Pageable pageable
    );

    Page<User> findByUserRole(
            UserRole role,
            Pageable pageable
    );

    Page<User> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userRole = :role")
    Page<User> findAllByUserRole(
            @Param("role") UserRole role,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userRole = :newRole WHERE u.id = :userId")
    int updateUserRole(
            @Param("userId") Long userId,
            @Param("newRole") UserRole newRole
    );

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userRole = :newRole WHERE u.username = :username")
    int updateUserRoleByUsername(
            @Param("username") String username,
            @Param("newRole") UserRole newRole
    );

    long countByUsernameContainingIgnoreCase(String username);

    long countByUserRole(UserRole role);

    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(
            LocalDateTime dateTime
    );
}
