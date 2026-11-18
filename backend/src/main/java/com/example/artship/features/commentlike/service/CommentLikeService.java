package com.example.artship.features.commentlike.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.comment.model.Comment;
import com.example.artship.entities.comment.repository.CommentRepository;
import com.example.artship.entities.commentlike.model.CommentLikes;
import com.example.artship.entities.commentlike.repository.CommentLikesRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;

@Service
public class CommentLikeService {

    private static final Logger log
            = LoggerFactory.getLogger(CommentLikeService.class);

    private final CommentLikesRepository commentLikesRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentLikeService(
            CommentLikesRepository commentLikesRepository,
            CommentRepository commentRepository,
            UserRepository userRepository
    ) {
        this.commentLikesRepository = commentLikesRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void likeComment(Long userId, Long commentId) {
        log.info("User {} likes comment {}", userId, commentId);

        if (commentLikesRepository.existsByUserIdAndCommentId(
                userId, commentId
        )) {
            throw new IllegalArgumentException(
                    "User already liked this comment"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found")
                );

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()
                        -> new IllegalArgumentException("Comment not found")
                );

        CommentLikes like = new CommentLikes(user, comment);

        commentLikesRepository.save(like);

        log.info("User {} liked comment {}", userId, commentId);
    }

    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        log.info("User {} unlikes comment {}", userId, commentId);

        if (!commentLikesRepository.existsByUserIdAndCommentId(
                userId, commentId
        )) {
            throw new IllegalArgumentException(
                    "User has not liked this comment"
            );
        }

        commentLikesRepository.deleteByUserIdAndCommentId(
                userId, commentId
        );

        log.info("User {} unliked comment {}", userId, commentId);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long userId, Long commentId) {
        return commentLikesRepository.existsByUserIdAndCommentId(
                userId, commentId
        );
    }

    @Transactional(readOnly = true)
    public long getLikesCount(Long commentId) {
        return commentLikesRepository.countByCommentId(commentId);
    }

    @Transactional
    public void deleteAllLikesByCommentId(Long commentId) {
        log.info("Deleting all likes for comment {}", commentId);

        commentLikesRepository.deleteByCommentId(commentId);
    }

    @Transactional
    public void deleteAllLikesByUserId(Long userId) {
        log.info("Deleting all likes for user {}", userId);

        commentLikesRepository.deleteByUserId(userId);
    }
}
