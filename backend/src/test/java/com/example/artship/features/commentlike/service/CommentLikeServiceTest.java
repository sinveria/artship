package com.example.artship.features.commentlike.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.artship.entities.comment.model.Comment;
import com.example.artship.entities.comment.repository.CommentRepository;
import com.example.artship.entities.commentlike.model.CommentLikes;
import com.example.artship.entities.commentlike.repository.CommentLikesRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

    @Mock
    private CommentLikesRepository commentLikesRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentLikeService commentLikeService;

    @Test
    void likeCommentShouldSaveLike() {
        User user = mock(User.class);
        Comment comment = mock(Comment.class);

        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.findById(2L))
                .thenReturn(Optional.of(comment));

        commentLikeService.likeComment(1L, 2L);

        verify(commentLikesRepository).save(any(CommentLikes.class));
    }

    @Test
    void likeCommentShouldThrowWhenLikeAlreadyExists() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeService.likeComment(1L, 2L)
        );

        verify(userRepository, never()).findById(anyLong());
        verify(commentRepository, never()).findById(anyLong());
        verify(commentLikesRepository, never()).save(any());
    }

    @Test
    void likeCommentShouldThrowWhenUserDoesNotExist() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeService.likeComment(1L, 2L)
        );

        verify(commentRepository, never()).findById(anyLong());
        verify(commentLikesRepository, never()).save(any());
    }

    @Test
    void likeCommentShouldThrowWhenCommentDoesNotExist() {
        User user = mock(User.class);

        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(false);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeService.likeComment(1L, 2L)
        );

        verify(commentLikesRepository, never()).save(any());
    }

    @Test
    void unlikeCommentShouldDeleteLike() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(true);

        commentLikeService.unlikeComment(1L, 2L);

        verify(commentLikesRepository)
                .deleteByUserIdAndCommentId(1L, 2L);
    }

    @Test
    void unlikeCommentShouldThrowWhenLikeDoesNotExist() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeService.unlikeComment(1L, 2L)
        );

        verify(commentLikesRepository, never())
                .deleteByUserIdAndCommentId(anyLong(), anyLong());
    }

    @Test
    void isLikedByUserShouldReturnTrue() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(true);

        assertTrue(commentLikeService.isLikedByUser(1L, 2L));
    }

    @Test
    void isLikedByUserShouldReturnFalse() {
        when(commentLikesRepository.existsByUserIdAndCommentId(1L, 2L))
                .thenReturn(false);

        assertFalse(commentLikeService.isLikedByUser(1L, 2L));
    }

    @Test
    void getLikesCountShouldReturnCount() {
        when(commentLikesRepository.countByCommentId(2L))
                .thenReturn(10L);

        assertEquals(10L, commentLikeService.getLikesCount(2L));
    }

    @Test
    void getLikesCountShouldReturnZeroWhenNoLikes() {
        when(commentLikesRepository.countByCommentId(2L))
                .thenReturn(0L);

        assertEquals(0L, commentLikeService.getLikesCount(2L));
    }

    @Test
    void deleteAllLikesByCommentIdShouldDeleteLikes() {
        commentLikeService.deleteAllLikesByCommentId(2L);

        verify(commentLikesRepository).deleteByCommentId(2L);
    }

    @Test
    void deleteAllLikesByUserIdShouldDeleteLikes() {
        commentLikeService.deleteAllLikesByUserId(1L);

        verify(commentLikesRepository).deleteByUserId(1L);
    }
}
