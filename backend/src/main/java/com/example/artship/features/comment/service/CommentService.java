package com.example.artship.features.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.comment.model.Comment;
import com.example.artship.entities.comment.repository.CommentRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.comment.dto.CommentDto;
import com.example.artship.features.comment.mapper.CommentMapper;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArtRepository artRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentService(
            CommentRepository commentRepository,
            ArtRepository artRepository,
            UserRepository userRepository,
            CommentMapper commentMapper
    ) {
        this.commentRepository = commentRepository;
        this.artRepository = artRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    public CommentDto createComment(String text, Long artId, Long userId) {
        validateText(text);

        Art art = artRepository.findById(artId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Арт не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Пользователь не найден"));

        Comment comment = new Comment(text.trim(), art, user);

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public CommentDto createReply(
            String text,
            Long parentCommentId,
            Long userId
    ) {
        validateText(text);

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Родительский комментарий не найден"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Пользователь не найден"));

        if (parentComment.isHidden()) {
            throw new IllegalArgumentException(
                    "Нельзя ответить на скрытый комментарий"
            );
        }

        Comment reply = new Comment(
                text.trim(),
                parentComment.getArt(),
                user
        );

        reply.setParentComment(parentComment);

        return commentMapper.toDto(commentRepository.save(reply));
    }

    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long id) {
        Comment comment = findComment(id);
        return commentMapper.toDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByArtId(Long artId) {
        if (!artRepository.existsById(artId)) {
            throw new IllegalArgumentException("Арт не найден");
        }

        return commentMapper.toDtos(
                commentRepository.findByArtIdAndHiddenFalse(artId)
        );
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getRootCommentsByArtId(Long artId) {
        if (!artRepository.existsById(artId)) {
            throw new IllegalArgumentException("Арт не найден");
        }

        return commentMapper.toDtos(
                commentRepository
                        .findByArtIdAndParentCommentIsNullAndHiddenFalse(artId)
        );
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getReplies(Long parentCommentId) {
        findComment(parentCommentId);

        return commentMapper.toDtos(
                commentRepository.findByParentCommentId(parentCommentId)
                        .stream()
                        .filter(comment -> !comment.isHidden())
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        return commentMapper.toDtos(
                commentRepository.findByUserId(userId)
                        .stream()
                        .filter(comment -> !comment.isHidden())
                        .toList()
        );
    }

    public CommentDto updateComment(
            Long commentId,
            Long userId,
            String newText
    ) {
        validateText(newText);

        Comment comment = findComment(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Нет прав на редактирование комментария"
            );
        }

        if (comment.isHidden()) {
            throw new IllegalArgumentException(
                    "Нельзя редактировать скрытый комментарий"
            );
        }

        comment.setText(newText.trim());

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public CommentDto hideComment(Long commentId) {
        Comment comment = findComment(commentId);

        comment.setHidden(true);

        return commentMapper.toDto(commentRepository.save(comment));
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findComment(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Нет прав на удаление комментария"
            );
        }

        deleteCommentTree(comment);
    }

    private void deleteCommentTree(Comment comment) {
        List<Comment> replies =
                commentRepository.findByParentCommentId(comment.getId());

        for (Comment reply : replies) {
            deleteCommentTree(reply);
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public long countCommentsByArtId(Long artId) {
        if (!artRepository.existsById(artId)) {
            throw new IllegalArgumentException("Арт не найден");
        }

        return commentRepository.countByArtIdAndHiddenFalse(artId);
    }

    private Comment findComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Комментарий не найден"));
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Текст комментария не может быть пустым"
            );
        }
    }
}