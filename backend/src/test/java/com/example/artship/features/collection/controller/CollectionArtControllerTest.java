package com.example.artship.features.collection.controller;

import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.dto.CollectionArtDto;
import com.example.artship.features.collection.dto.CopyArtRequest;
import com.example.artship.features.collection.dto.MoveArtRequest;
import com.example.artship.features.collection.service.CollectionArtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionArtControllerTest {

    @Mock
    private CollectionArtService collectionArtService;

    @InjectMocks
    private CollectionArtController collectionArtController;

    @Test
    void addArtToCollectionShouldReturnCreated() {
        CollectionArtDto dto = new CollectionArtDto();
        dto.setCollectionId(1L);
        dto.setArtId(2L);

        when(collectionArtService.addArtToCollection(1L, 2L))
                .thenReturn(dto);

        ResponseEntity<?> response =
                collectionArtController.addArtToCollection(1L, 2L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
        assertEquals(
                "/api/collection-arts/collection/1/art/2",
                response.getHeaders().getLocation().toString()
        );
    }

    @Test
    void addArtToCollectionShouldReturnBadRequestWhenServiceFails() {
        when(collectionArtService.addArtToCollection(1L, 2L))
                .thenThrow(new IllegalArgumentException("Art already exists"));

        ResponseEntity<?> response =
                collectionArtController.addArtToCollection(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals(
                "Art already exists",
                ((Map<?, ?>) response.getBody()).get("error")
        );
    }

    @Test
    void removeArtFromCollectionShouldReturnNoContent() {
        ResponseEntity<Void> response =
                collectionArtController.removeArtFromCollection(1L, 2L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(collectionArtService).removeArtFromCollection(1L, 2L);
    }

    @Test
    void removeArtFromCollectionShouldReturnNotFoundWhenServiceFails() {
        doThrow(new IllegalArgumentException("Collection not found"))
                .when(collectionArtService)
                .removeArtFromCollection(1L, 2L);

        ResponseEntity<Void> response =
                collectionArtController.removeArtFromCollection(1L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getArtsByCollectionShouldReturnPage() {
        Page<ArtDto> page = Page.empty();
        when(collectionArtService.getArtsByCollectionId(
                eq(1L), any()
        )).thenReturn(page);

        ResponseEntity<Page<ArtDto>> response =
                collectionArtController.getArtsByCollection(
                        1L, PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void getCollectionsByArtShouldReturnPage() {
        Page<CollectionArtDto> page = Page.empty();
        when(collectionArtService.getCollectionsByArtId(
                eq(2L), any(PageRequest.class)
        )).thenReturn(page);

        ResponseEntity<Page<CollectionArtDto>> response =
                collectionArtController.getCollectionsByArt(
                        2L, PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void getCollectionArtsShouldReturnPage() {
        Page<CollectionArtDto> page = Page.empty();
        when(collectionArtService.getCollectionArtDtosByCollectionId(
                eq(1L), any(PageRequest.class)
        )).thenReturn(page);

        ResponseEntity<Page<CollectionArtDto>> response =
                collectionArtController.getCollectionArts(
                        1L, PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void getArtCountByCollectionShouldReturnCount() {
        when(collectionArtService.getArtCountByCollectionId(1L))
                .thenReturn(7L);

        ResponseEntity<Long> response =
                collectionArtController.getArtCountByCollection(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7L, response.getBody());
    }

    @Test
    void checkArtInCollectionShouldReturnTrueWhenExists() {
        when(collectionArtService.existsByCollectionIdAndArtId(1L, 2L))
                .thenReturn(true);

        ResponseEntity<Boolean> response =
                collectionArtController.checkArtInCollection(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
    }

    @Test
    void checkArtInCollectionShouldReturnFalseWhenMissing() {
        when(collectionArtService.existsByCollectionIdAndArtId(1L, 2L))
                .thenReturn(false);

        ResponseEntity<Boolean> response =
                collectionArtController.checkArtInCollection(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody());
    }

    @Test
    void moveArtBetweenCollectionsShouldReturnOk() {
        MoveArtRequest request = new MoveArtRequest();
        request.setFromCollectionId(1L);
        request.setToCollectionId(3L);

        ResponseEntity<Void> response =
                collectionArtController.moveArtBetweenCollections(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(collectionArtService)
                .moveArtBetweenCollections(2L, 1L, 3L);
    }

    @Test
    void moveArtBetweenCollectionsShouldReturnBadRequestWhenServiceFails() {
        MoveArtRequest request = new MoveArtRequest();
        request.setFromCollectionId(1L);
        request.setToCollectionId(1L);

        doThrow(new IllegalArgumentException("Collections are the same"))
                .when(collectionArtService)
                .moveArtBetweenCollections(2L, 1L, 1L);

        ResponseEntity<Void> response =
                collectionArtController.moveArtBetweenCollections(2L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void copyArtToCollectionShouldReturnDto() {
        CopyArtRequest request = new CopyArtRequest();
        request.setCollectionId(3L);

        CollectionArtDto dto = new CollectionArtDto();
        dto.setCollectionId(3L);
        dto.setArtId(2L);

        when(collectionArtService.copyArtToCollection(2L, 3L))
                .thenReturn(dto);

        ResponseEntity<CollectionArtDto> response =
                collectionArtController.copyArtToCollection(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void copyArtToCollectionShouldReturnBadRequestWhenServiceFails() {
        CopyArtRequest request = new CopyArtRequest();
        request.setCollectionId(3L);

        when(collectionArtService.copyArtToCollection(2L, 3L))
                .thenThrow(new IllegalArgumentException("Art already exists"));

        ResponseEntity<CollectionArtDto> response =
                collectionArtController.copyArtToCollection(2L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}
