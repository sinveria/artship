package com.example.artship.features.commentlike.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.artship.features.commentlike.service.CommentLikeService;

class CommentLikeControllerTest {

    private CommentLikeService commentLikeService;
    private CommentLikeController commentLikeController;

    @BeforeEach
    void setUp() {
        commentLikeService = mock(CommentLikeService.class);
        commentLikeController
                = new CommentLikeController(commentLikeService);
    }

    @Test
    void likeCommentShouldReturnNoContent() {
        ResponseEntity<Void> response
                = commentLikeController.likeComment(2L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(commentLikeService).likeComment(1L, 2L);
    }

    @Test
    void likeCommentShouldPropagateServiceException() {
        doThrow(new IllegalArgumentException("Comment not found"))
                .when(commentLikeService).likeComment(1L, 2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeController.likeComment(2L, 1L)
        );

        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    void unlikeCommentShouldReturnNoContent() {
        ResponseEntity<Void> response
                = commentLikeController.unlikeComment(2L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(commentLikeService).unlikeComment(1L, 2L);
    }

    @Test
    void unlikeCommentShouldPropagateServiceException() {
        doThrow(new IllegalArgumentException(
                "User has not liked this comment"
        )).when(commentLikeService).unlikeComment(1L, 2L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentLikeController.unlikeComment(2L, 1L)
        );

        assertEquals(
                "User has not liked this comment",
                exception.getMessage()
        );
    }

    @Test
    void isLikedByUserShouldReturnTrue() {
        when(commentLikeService.isLikedByUser(1L, 2L))
                .thenReturn(true);

        ResponseEntity<Boolean> response
                = commentLikeController.isLikedByUser(2L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void isLikedByUserShouldReturnFalse() {
        when(commentLikeService.isLikedByUser(1L, 2L))
                .thenReturn(false);

        ResponseEntity<Boolean> response
                = commentLikeController.isLikedByUser(2L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }

    @Test
    void getLikesCountShouldReturnCount() {
        when(commentLikeService.getLikesCount(2L))
                .thenReturn(15L);

        ResponseEntity<Long> response
                = commentLikeController.getLikesCount(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15L, response.getBody());
    }

    @Test
    void getLikesCountShouldReturnZeroWhenNoLikes() {
        when(commentLikeService.getLikesCount(2L))
                .thenReturn(0L);

        ResponseEntity<Long> response
                = commentLikeController.getLikesCount(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0L, response.getBody());
    }
}
