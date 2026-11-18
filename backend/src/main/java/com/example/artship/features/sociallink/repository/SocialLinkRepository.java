package com.example.artship.features.sociallink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.user.model.User;
import com.example.artship.features.sociallink.model.SocialLink;
import com.example.artship.features.sociallink.model.SocialPlatform;

@Repository
public interface SocialLinkRepository
        extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByUserOrderByDisplayOrderAsc(User user);

    List<SocialLink> findByUserAndVisibleTrueOrderByDisplayOrderAsc(
            User user
    );

    Optional<SocialLink> findByUserAndPlatform(
            User user,
            SocialPlatform platform
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM SocialLink s WHERE s.user = :user")
    void deleteAllByUser(@Param("user") User user);

    boolean existsByUserAndPlatform(
            User user,
            SocialPlatform platform
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE SocialLink s
            SET s.displayOrder = :order
            WHERE s.id = :id
            """)
    void updateDisplayOrder(
            @Param("id") Long id,
            @Param("order") Integer order
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM SocialLink s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
