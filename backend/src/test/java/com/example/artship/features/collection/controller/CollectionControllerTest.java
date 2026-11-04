package com.example.artship.features.collection.controller;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;
import com.example.artship.features.collection.dto.CollectionDto;
import com.example.artship.features.collection.service.CollectionService;
import com.example.artship.features.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    @Mock
    private CollectionService collectionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CollectionController collectionController;

    private User owner;
    private CollectionDto publicCollection;
    private CollectionDto privateCollection;
    private Principal ownerPrincipal;

    @BeforeEach
    void setUp() {
        owner = mock(User.class);
        ownerPrincipal = () -> "owner";

        publicCollection = new CollectionDto();
        publicCollection.setId(10L);
        publicCollection.setTitle("Public collection");
        publicCollection.setIsPublic(true);
        publicCollection.setUserId(1L);

        privateCollection = new CollectionDto();
        privateCollection.setId(11L);
        privateCollection.setTitle("Private collection");
        privateCollection.setIsPublic(false);
        privateCollection.setUserId(1L);
    }

    private void stubOwner() {
        when(owner.getId()).thenReturn(1L);
        when(owner.getUserRole()).thenReturn(UserRole.USER);
    }

    @Test
    void createCollectionShouldReturnCreatedForAuthenticatedUser() {
        when(owner.getId()).thenReturn(1L);
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.createCollection(
                "My collection", "Description", true, null, 1L
        )).thenReturn(publicCollection);

        ResponseEntity<CollectionDto> response =
                collectionController.createCollection(
                        "My collection",
                        "Description",
                        true,
                        null,
                        ownerPrincipal
                );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(publicCollection, response.getBody());
    }

    @Test
    void createCollectionShouldRejectAnonymousUser() {
        ResponseEntity<CollectionDto> response =
                collectionController.createCollection(
                        "My collection", null, true, null, null
                );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(collectionService);
    }

    @Test
    void createCollectionShouldRejectBlankTitle() {
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));

        ResponseEntity<CollectionDto> response =
                collectionController.createCollection(
                        "  ", null, true, null, ownerPrincipal
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(collectionService, never())
                .createCollection(anyString(), any(), any(), any(), anyLong());
    }

    @Test
    void createCollectionShouldReturnBadRequestWhenServiceFails() {
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.createCollection(
                anyString(), any(), any(), any(), anyLong()
        )).thenThrow(new IllegalArgumentException("Duplicate title"));

        ResponseEntity<CollectionDto> response =
                collectionController.createCollection(
                        "My collection", null, true, null, ownerPrincipal
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateCollectionShouldAllowOwner() {
        stubOwner();
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));
        when(collectionService.updateCollection(
                10L, "Updated", null, null, null
        )).thenReturn(publicCollection);

        ResponseEntity<CollectionDto> response =
                collectionController.updateCollection(
                        10L, "Updated", null, null, null, ownerPrincipal
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(collectionService).updateCollection(
                10L, "Updated", null, null, null
        );
    }

    @Test
    void updateCollectionShouldRejectAnonymousUser() {
        ResponseEntity<CollectionDto> response =
                collectionController.updateCollection(
                        10L, "Updated", null, null, null, null
                );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(collectionService, never())
                .updateCollection(anyLong(), any(), any(), any(), any());
    }

    @Test
    void updateCollectionShouldRejectAnotherUser() {
        User anotherUser = mock(User.class);
        when(anotherUser.getId()).thenReturn(2L);
        when(anotherUser.getUserRole()).thenReturn(UserRole.USER);
        when(userService.findByUsername("another"))
                .thenReturn(Optional.of(anotherUser));
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));

        ResponseEntity<CollectionDto> response =
                collectionController.updateCollection(
                        10L, "Updated", null, null, null,
                        () -> "another"
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(collectionService, never())
                .updateCollection(anyLong(), any(), any(), any(), any());
    }

    @Test
    void updateCollectionShouldAllowAdmin() {
        User admin = mock(User.class);
        when(admin.getUserRole()).thenReturn(UserRole.ADMIN);
        when(userService.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));
        when(collectionService.updateCollection(
                10L, "Updated", null, null, null
        )).thenReturn(publicCollection);

        ResponseEntity<CollectionDto> response =
                collectionController.updateCollection(
                        10L, "Updated", null, null, null,
                        () -> "admin"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCollectionShouldReturnPublicCollectionWithoutPrincipal() {
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));

        ResponseEntity<CollectionDto> response =
                collectionController.getCollection(10L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(publicCollection, response.getBody());
    }

    @Test
    void getCollectionShouldRejectPrivateCollectionWithoutPrincipal() {
        when(collectionService.getCollectionDtoById(11L))
                .thenReturn(Optional.of(privateCollection));

        ResponseEntity<CollectionDto> response =
                collectionController.getCollection(11L, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getCollectionShouldAllowOwnerToViewPrivateCollection() {
        stubOwner();
        when(collectionService.getCollectionDtoById(11L))
                .thenReturn(Optional.of(privateCollection));
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));

        ResponseEntity<CollectionDto> response =
                collectionController.getCollection(11L, ownerPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getCollectionShouldReturnNotFoundWhenMissing() {
        when(collectionService.getCollectionDtoById(404L))
                .thenReturn(Optional.empty());

        ResponseEntity<CollectionDto> response =
                collectionController.getCollection(404L, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserCollectionsShouldReturnAllForOwner() {
        stubOwner();
        Page<CollectionDto> page = new PageImpl<>(List.of(publicCollection));
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.getCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        )).thenReturn(page);

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.getUserCollections(
                        1L, PageRequest.of(0, 20), ownerPrincipal
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(collectionService).getCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        );
    }

    @Test
    void getUserCollectionsShouldReturnOnlyPublicForAnonymousUser() {
        Page<CollectionDto> page = new PageImpl<>(List.of(publicCollection));
        when(collectionService.getPublicCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        )).thenReturn(page);

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.getUserCollections(
                        1L, PageRequest.of(0, 20), null
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(collectionService).getPublicCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        );
        verify(collectionService, never()).getCollectionsByUserId(
                anyLong(), any(PageRequest.class)
        );
    }

    @Test
    void getUserCollectionsShouldReturnAllForModerator() {
        User moderator = mock(User.class);
        when(moderator.getUserRole()).thenReturn(UserRole.MODERATOR);
        when(userService.findByUsername("moderator"))
                .thenReturn(Optional.of(moderator));
        when(collectionService.getCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        )).thenReturn(Page.empty());

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.getUserCollections(
                        1L, PageRequest.of(0, 20), () -> "moderator"
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(collectionService).getCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        );
    }

    @Test
    void getUserPublicCollectionsShouldReturnPage() {
        when(collectionService.getPublicCollectionsByUserId(
                eq(1L), any(PageRequest.class)
        )).thenReturn(Page.empty());

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.getUserPublicCollections(
                        1L, PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getPublicCollectionsShouldReturnPage() {
        when(collectionService.getPublicCollections(
                any(PageRequest.class)
        )).thenReturn(Page.empty());

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.getPublicCollections(
                        PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void searchCollectionsShouldRejectBlankQuery() {
        ResponseEntity<Page<CollectionDto>> response =
                collectionController.searchCollections(
                        "  ", PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(collectionService, never())
                .searchPublicCollections(anyString(), any(PageRequest.class));
    }

    @Test
    void searchCollectionsShouldReturnResults() {
        when(collectionService.searchPublicCollections(
                eq("landscape"), any(PageRequest.class)
        )).thenReturn(Page.empty());

        ResponseEntity<Page<CollectionDto>> response =
                collectionController.searchCollections(
                        " landscape ", PageRequest.of(0, 20)
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(collectionService).searchPublicCollections(
                eq("landscape"), any(PageRequest.class)
        );
    }

    @Test
    void deleteCollectionShouldAllowOwner() {
        stubOwner();
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));

        ResponseEntity<Void> response =
                collectionController.deleteCollection(10L, ownerPrincipal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(collectionService).deleteCollection(10L);
    }

    @Test
    void deleteCollectionShouldRejectAnotherUser() {
        User anotherUser = mock(User.class);
        when(anotherUser.getId()).thenReturn(2L);
        when(anotherUser.getUserRole()).thenReturn(UserRole.USER);
        when(userService.findByUsername("another"))
                .thenReturn(Optional.of(anotherUser));
        when(collectionService.getCollectionDtoById(10L))
                .thenReturn(Optional.of(publicCollection));

        ResponseEntity<Void> response =
                collectionController.deleteCollection(
                        10L, () -> "another"
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(collectionService, never()).deleteCollection(anyLong());
    }

    @Test
    void deleteCollectionShouldRejectAnonymousUser() {
        ResponseEntity<Void> response =
                collectionController.deleteCollection(10L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(collectionService, never()).deleteCollection(anyLong());
    }

    @Test
    void deleteCollectionShouldReturnNotFoundWhenMissing() {
        when(userService.findByUsername("owner"))
                .thenReturn(Optional.of(owner));
        when(collectionService.getCollectionDtoById(404L))
                .thenReturn(Optional.empty());

        ResponseEntity<Void> response =
                collectionController.deleteCollection(404L, ownerPrincipal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(collectionService, never()).deleteCollection(anyLong());
    }
}
