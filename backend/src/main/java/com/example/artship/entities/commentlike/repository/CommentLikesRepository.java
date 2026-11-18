package com.example.artship.entities.commentlike.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.commentlike.model.CommentLikes;

@Repository
public interface CommentLikesRepository
        extends JpaRepository<CommentLikes, CommentLikes.CommentLikeId> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Page<CommentLikes> findByCommentId(Long commentId, Pageable pageable);

    Page<CommentLikes> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CommentLikes cl
            WHERE cl.user.id = :userId
              AND cl.comment.id = :commentId
            """)
    void deleteByUserIdAndCommentId(
            @Param("userId") Long userId,
            @Param("commentId") Long commentId
    );

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CommentLikes cl
            WHERE cl.comment.id = :commentId
            """)
    void deleteByCommentId(@Param("commentId") Long commentId);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CommentLikes cl
            WHERE cl.user.id = :userId
            """)
    void deleteByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(cl)
            FROM CommentLikes cl
            WHERE cl.comment.id = :commentId
            """)
    long countByCommentId(@Param("commentId") Long commentId);
}
