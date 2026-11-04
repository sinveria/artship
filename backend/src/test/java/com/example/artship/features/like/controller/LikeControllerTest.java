package com.example.artship.features.like.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.artship.features.like.dto.LikeDto;
import com.example.artship.features.like.service.LikeService;

class LikeControllerTest {

    private LikeService likeService;
    private LikeController likeController;

    @BeforeEach
    void setUp() {
        likeService = mock(LikeService.class);
        likeController = new LikeController(likeService);
    }

    @Test
    void addLikeShouldReturnLikeDto() {
        LikeDto likeDto = new LikeDto();

        when(likeService.addLike(1L, 2L)).thenReturn(likeDto);

        ResponseEntity<?> response = likeController.addLike(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(likeDto, response.getBody());

        verify(likeService).addLike(1L, 2L);
    }

    @Test
    void addLikeShouldReturnBadRequestWhenServiceThrowsException() {
        when(likeService.addLike(1L, 2L))
                .thenThrow(new IllegalArgumentException("Like already exists"));

        ResponseEntity<?> response = likeController.addLike(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Like already exists", body.get("error"));
    }

    @Test
    void removeLikeShouldReturnNoContent() {
        doNothing().when(likeService).removeLike(1L, 2L);

        ResponseEntity<Void> response = likeController.removeLike(1L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(likeService).removeLike(1L, 2L);
    }

    @Test
    void removeLikeShouldReturnNotFoundWhenServiceThrowsException() {
        doThrow(new IllegalArgumentException("Like does not exist"))
                .when(likeService).removeLike(1L, 2L);

        ResponseEntity<Void> response = likeController.removeLike(1L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void isLikedShouldReturnTrue() {
        when(likeService.isLiked(1L, 2L)).thenReturn(true);

        ResponseEntity<Boolean> response = likeController.isLiked(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void isLikedShouldReturnFalse() {
        when(likeService.isLiked(1L, 2L)).thenReturn(false);

        ResponseEntity<Boolean> response = likeController.isLiked(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }

    @Test
    void getLikesByArtShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        LikeDto likeDto = new LikeDto();
        Page<LikeDto> page = new PageImpl<>(
                List.of(likeDto),
                pageable,
                1
        );

        when(likeService.getLikesByArtId(2L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<LikeDto>> response
                = likeController.getLikesByArt(2L, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(likeService).getLikesByArtId(2L, pageable);
    }

    @Test
    void getLikesByArtShouldReturnEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<LikeDto> page = Page.empty(pageable);

        when(likeService.getLikesByArtId(2L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<LikeDto>> response
                = likeController.getLikesByArt(2L, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getLikeCountByArtShouldReturnCount() {
        when(likeService.getLikeCountByArtId(2L)).thenReturn(15L);

        ResponseEntity<Long> response
                = likeController.getLikeCountByArt(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15L, response.getBody());
    }

    @Test
    void getLikeCountByArtShouldReturnZeroWhenThereAreNoLikes() {
        when(likeService.getLikeCountByArtId(2L)).thenReturn(0L);

        ResponseEntity<Long> response
                = likeController.getLikeCountByArt(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0L, response.getBody());
    }
}
