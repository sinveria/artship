package com.example.artship.features.collection.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.repository.CollectionArtRepository;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.collection.dto.CollectionDto;
import com.example.artship.features.collection.mapper.CollectionMapper;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionArtRepository collectionArtRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionMapper collectionMapper;

    @InjectMocks
    private CollectionService collectionService;

    private User user;
    private Collection collection;
    private CollectionDto collectionDto;

    @BeforeEach
    void setUp() {
        user = mock(User.class);

        collection = new Collection("My collection", user);

        ReflectionTestUtils.setField(collection, "id", 1L);

        collection.setDescription("Collection description");
        collection.setIsPublic(true);

        collectionDto = new CollectionDto();
        collectionDto.setId(1L);
        collectionDto.setTitle("My collection");
        collectionDto.setDescription("Collection description");
        collectionDto.setIsPublic(true);
        collectionDto.setUserId(1L);
        collectionDto.setArtCount(0);
    }

    @Test
    void createCollectionShouldSaveAndReturnDto() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(collectionRepository.existsByUserIdAndTitle(
                1L, "My collection"
        )).thenReturn(false);

        when(collectionRepository.save(any(Collection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(collectionMapper.toDto(any(Collection.class)))
                .thenReturn(collectionDto);

        CollectionDto result = collectionService.createCollection(
                "My collection",
                "Collection description",
                true,
                null,
                1L
        );

        assertNotNull(result);
        assertEquals("My collection", result.getTitle());

        verify(collectionRepository).save(any(Collection.class));
    }

    @Test
    void createCollectionShouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.createCollection(
                        "My collection",
                        "Description",
                        true,
                        null,
                        999L
                )
        );

        verify(collectionRepository, never())
                .save(any(Collection.class));
    }

    @Test
    void createCollectionShouldThrowWhenTitleAlreadyExists() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(collectionRepository.existsByUserIdAndTitle(
                1L, "My collection"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.createCollection(
                        "My collection",
                        "Description",
                        true,
                        null,
                        1L
                )
        );

        verify(collectionRepository, never())
                .save(any(Collection.class));
    }

    @Test
    void getCollectionByIdShouldReturnCollection() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        Optional<Collection> result
                = collectionService.getCollectionById(1L);

        assertTrue(result.isPresent());
        assertEquals("My collection", result.get().getTitle());
    }

    @Test
    void getCollectionDtoByIdShouldReturnDto() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(collectionMapper.toDto(collection))
                .thenReturn(collectionDto);

        when(collectionArtRepository.countByCollectionIdAndArtStatus(
                anyLong(), any()
        )).thenReturn(0L);

        Optional<CollectionDto> result
                = collectionService.getCollectionDtoById(1L);

        assertTrue(result.isPresent());
        assertEquals("My collection", result.get().getTitle());
        assertEquals(0, result.get().getArtCount());
    }

    @Test
    void getCollectionDtoByIdShouldReturnEmptyWhenMissing() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<CollectionDto> result
                = collectionService.getCollectionDtoById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateCollectionShouldUpdateFields() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(user.getId()).thenReturn(1L);

        when(collectionRepository.existsByUserIdAndTitle(
                1L, "Updated collection"
        )).thenReturn(false);

        when(collectionRepository.save(collection))
                .thenReturn(collection);

        when(collectionMapper.toDto(collection))
                .thenReturn(collectionDto);

        CollectionDto result = collectionService.updateCollection(
                1L,
                "Updated collection",
                "Updated description",
                false,
                "cover.jpg"
        );

        assertNotNull(result);
        assertEquals("Updated collection", collection.getTitle());
        assertEquals("Updated description", collection.getDescription());
        assertFalse(collection.getIsPublic());
        assertEquals("cover.jpg", collection.getCoverImageUrl());

        verify(collectionRepository).save(collection);
    }

    @Test
    void updateCollectionShouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.updateCollection(
                        999L,
                        "Updated",
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void updateCollectionShouldThrowWhenTitleAlreadyExists() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(user.getId()).thenReturn(1L);

        when(collectionRepository.existsByUserIdAndTitle(
                1L, "Another collection"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.updateCollection(
                        1L,
                        "Another collection",
                        null,
                        null,
                        null
                )
        );

        verify(collectionRepository, never()).save(any());
    }

    @Test
    void deleteCollectionShouldDeleteCollection() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        collectionService.deleteCollection(1L);

        verify(collectionRepository).delete(collection);
        verify(collectionArtRepository).deleteByCollectionId(1L);
    }

    @Test
    void deleteCollectionShouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.deleteCollection(999L)
        );

        verify(collectionRepository, never()).delete(any());
    }

    @Test
    void deleteCollectionShouldRejectLikedCollection() {
        Collection likedCollection
                = new Collection("Понравившиеся", user);

        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(likedCollection));

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionService.deleteCollection(1L)
        );

        verify(collectionRepository, never()).delete(any());
    }

    @Test
    void getCollectionsByUserIdShouldReturnPage() {
        Page<Collection> page = new PageImpl<>(
                List.of(collection),
                PageRequest.of(0, 10),
                1
        );

        when(collectionRepository.findByUserIdOrderByCreatedAtDesc(
                eq(1L), any()
        )).thenReturn(page);

        when(collectionMapper.toDto(collection))
                .thenReturn(collectionDto);

        when(collectionArtRepository.countByCollectionIdAndArtStatus(
                anyLong(), any()
        )).thenReturn(0L);

        Page<CollectionDto> result
                = collectionService.getCollectionsByUserId(
                        1L,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("My collection", result.getContent().get(0).getTitle());
    }

    @Test
    void getPublicCollectionsShouldReturnPage() {
        Page<Collection> page = new PageImpl<>(
                List.of(collection),
                PageRequest.of(0, 10),
                1
        );

        when(collectionRepository.findByIsPublicTrueOrderByCreatedAtDesc(
                any()
        )).thenReturn(page);

        when(collectionMapper.toDto(collection))
                .thenReturn(collectionDto);

        when(collectionArtRepository.countByCollectionIdAndArtStatus(
                anyLong(), any()
        )).thenReturn(0L);

        Page<CollectionDto> result
                = collectionService.getPublicCollections(
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchPublicCollectionsShouldReturnPage() {
        Page<Collection> page = new PageImpl<>(
                List.of(collection),
                PageRequest.of(0, 10),
                1
        );

        when(collectionRepository.searchPublicCollections(
                eq("My"), any()
        )).thenReturn(page);

        when(collectionMapper.toDto(collection))
                .thenReturn(collectionDto);

        when(collectionArtRepository.countByCollectionIdAndArtStatus(
                anyLong(), any()
        )).thenReturn(0L);

        Page<CollectionDto> result
                = collectionService.searchPublicCollections(
                        "My",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void isUserOwnerOfCollectionShouldReturnTrue() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(user.getId()).thenReturn(1L);

        assertTrue(
                collectionService.isUserOwnerOfCollection(1L, 1L)
        );
    }

    @Test
    void isUserOwnerOfCollectionShouldReturnFalseForAnotherUser() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(user.getId()).thenReturn(1L);

        assertFalse(
                collectionService.isUserOwnerOfCollection(1L, 2L)
        );
    }

    @Test
    void isUserOwnerOfCollectionShouldReturnFalseWhenMissing() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertFalse(
                collectionService.isUserOwnerOfCollection(999L, 1L)
        );
    }

    @Test
    void deleteAllUserCollectionsShouldDeleteUserCollections() {
        when(collectionRepository.findByUserId(1L))
                .thenReturn(List.of(collection));

        collectionService.deleteAllUserCollections(1L);

        verify(collectionRepository).deleteByUserId(1L);
    }

    @Test
    void deleteAllUserCollectionsShouldNotDeleteWhenEmpty() {
        when(collectionRepository.findByUserId(1L))
                .thenReturn(Collections.emptyList());

        collectionService.deleteAllUserCollections(1L);

        verify(collectionRepository, never()).deleteByUserId(anyLong());
    }
}
