
package com.example.artship.entities.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.artship.entities.comment.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArtId(Long artId);

    Page<Comment> findByArtId(Long artId, Pageable pageable);

    List<Comment> findByArtIdAndHiddenFalse(Long artId);

    Page<Comment> findByArtIdAndHiddenFalse(
            Long artId,
            Pageable pageable
    );

    List<Comment> findByUserId(Long userId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByArtIdAndParentCommentIsNull(Long artId);

    List<Comment> findByArtIdAndParentCommentIsNullAndHiddenFalse(
            Long artId
    );

    long countByArtId(Long artId);

    long countByArtIdAndHiddenFalse(Long artId);

    long countByUserId(Long userId);

    long countByParentCommentId(Long parentCommentId);

    boolean existsByIdAndUserId(Long id, Long userId);
}