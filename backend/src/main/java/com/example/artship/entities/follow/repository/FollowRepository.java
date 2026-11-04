package com.example.artship.entities.follow.repository;

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

import com.example.artship.entities.follow.model.Follow;

@Repository
public interface FollowRepository
        extends JpaRepository<Follow, Follow.FollowId> {

    Optional<Follow> findByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );

    boolean existsByFollowerIdAndFollowingId(
            Long followerId,
            Long followingId
    );

    List<Follow> findByFollowingId(Long followingId);

    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    List<Follow> findByFollowerId(Long followerId);

    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);

    @Query("""
            SELECT f
            FROM Follow f
            WHERE f.following.id = :userId
              AND LOWER(f.follower.username)
                  LIKE LOWER(CONCAT('%', :username, '%'))
            """)
    Page<Follow> searchFollowersByUsername(
            @Param("userId") Long userId,
            @Param("username") String username,
            Pageable pageable
    );

    @Query("""
            SELECT f
            FROM Follow f
            WHERE f.follower.id = :userId
              AND LOWER(f.following.username)
                  LIKE LOWER(CONCAT('%', :username, '%'))
            """)
    Page<Follow> searchFollowingByUsername(
            @Param("userId") Long userId,
            @Param("username") String username,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.following.id = :userId
              AND LOWER(f.follower.username)
                  LIKE LOWER(CONCAT('%', :username, '%'))
            """)
    long countFollowersByUsername(
            @Param("userId") Long userId,
            @Param("username") String username
    );

    @Query("""
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.follower.id = :userId
              AND LOWER(f.following.username)
                  LIKE LOWER(CONCAT('%', :username, '%'))
            """)
    long countFollowingByUsername(
            @Param("userId") Long userId,
            @Param("username") String username
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.follower.id = :userId")
    void deleteAllByFollowerId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.following.id = :userId")
    void deleteAllByFollowingId(@Param("userId") Long userId);
}
