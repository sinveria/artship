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

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.model.CollectionArt;
import com.example.artship.entities.collection.repository.CollectionArtRepository;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.features.collection.dto.CollectionArtDto;
import com.example.artship.features.collection.mapper.CollectionArtMapper;

@ExtendWith(MockitoExtension.class)
class CollectionArtServiceTest {

    @Mock
    private CollectionArtRepository collectionArtRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private ArtRepository artRepository;

    @Mock
    private CollectionArtMapper collectionArtMapper;

    @InjectMocks
    private CollectionArtService collectionArtService;

    private Collection collection;
    private Art art;
    private CollectionArt collectionArt;
    private CollectionArtDto collectionArtDto;

    @BeforeEach
    void setUp() {
        collection = mock(Collection.class);
        art = mock(Art.class);
        collectionArt = mock(CollectionArt.class);

        collectionArtDto = new CollectionArtDto();
        collectionArtDto.setCollectionId(1L);
        collectionArtDto.setArtId(1L);
    }

    @Test
    void addArtToCollectionShouldSaveAndReturnDto() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        when(art.getIsPublicFlag()).thenReturn(true);
        when(art.getStatus()).thenReturn(ArtStatus.ACTIVE);
        when(collection.getCoverImageUrl()).thenReturn(null);
        when(art.getImageUrl()).thenReturn("image.jpg");

        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(false);

        when(collectionArtRepository.save(any(CollectionArt.class)))
                .thenReturn(collectionArt);

        when(collectionArtMapper.toDto(collectionArt))
                .thenReturn(collectionArtDto);

        CollectionArtDto result
                = collectionArtService.addArtToCollection(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getCollectionId());
        assertEquals(1L, result.getArtId());

        verify(collectionArtRepository).save(any(CollectionArt.class));
        verify(collectionRepository).save(collection);
    }

    @Test
    void addArtToCollectionShouldThrowWhenCollectionNotFound() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.addArtToCollection(999L, 1L)
        );

        verify(collectionArtRepository, never())
                .save(any(CollectionArt.class));
    }

    @Test
    void addArtToCollectionShouldThrowWhenArtNotFound() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.addArtToCollection(1L, 999L)
        );

        verify(collectionArtRepository, never())
                .save(any(CollectionArt.class));
    }

    @Test
    void addArtToCollectionShouldRejectPrivateArt() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        when(art.getIsPublicFlag()).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.addArtToCollection(1L, 1L)
        );

        verify(collectionArtRepository, never())
                .save(any(CollectionArt.class));
    }

    @Test
    void addArtToCollectionShouldRejectInactiveArt() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        when(art.getIsPublicFlag()).thenReturn(true);
        when(art.getStatus()).thenReturn(ArtStatus.HIDDEN);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.addArtToCollection(1L, 1L)
        );

        verify(collectionArtRepository, never())
                .save(any(CollectionArt.class));
    }

    @Test
    void addArtToCollectionShouldRejectDuplicateArt() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        when(art.getIsPublicFlag()).thenReturn(true);
        when(art.getStatus()).thenReturn(ArtStatus.ACTIVE);

        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.addArtToCollection(1L, 1L)
        );

        verify(collectionArtRepository, never())
                .save(any(CollectionArt.class));
    }

    @Test
    void removeArtFromCollectionShouldDeleteArtAndUpdateCover()
            throws Exception {

        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(true);

        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(collection.getCoverImageUrl()).thenReturn("old-cover.jpg");

        when(collectionArtRepository.findByCollectionId(1L))
                .thenReturn(Collections.emptyList());

        collectionArtService.removeArtFromCollection(1L, 1L);

        verify(collectionArtRepository)
                .deleteByCollectionIdAndArtId(1L, 1L);

        verify(collection).setCoverImageUrl(null);
        verify(collectionRepository).save(collection);
    }

    @Test
    void removeArtFromCollectionShouldDoNothingWhenArtMissing() {
        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 999L
        )).thenReturn(false);

        collectionArtService.removeArtFromCollection(1L, 999L);

        verify(collectionArtRepository, never())
                .deleteByCollectionIdAndArtId(anyLong(), anyLong());

        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void removeAllArtsFromCollectionShouldDeleteAllArts() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(collectionArtRepository.countByCollectionId(1L))
                .thenReturn(3L);

        collectionArtService.removeAllArtsFromCollection(1L);

        verify(collectionArtRepository).deleteByCollectionId(1L);
        verify(collection).setCoverImageUrl(null);
        verify(collectionRepository).save(collection);
    }

    @Test
    void removeAllArtsFromCollectionShouldThrowWhenCollectionMissing() {
        when(collectionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.removeAllArtsFromCollection(999L)
        );

        verify(collectionArtRepository, never())
                .deleteByCollectionId(anyLong());
    }

    @Test
    void getArtsByCollectionIdShouldReturnEmptyPage() {
        Page<CollectionArt> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(collectionArtRepository.findByCollectionIdAndArtStatus(
                eq(1L),
                eq(ArtStatus.ACTIVE),
                any()
        )).thenReturn(page);

        Page<?> result = collectionArtService.getArtsByCollectionId(
                1L,
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getCollectionsByArtIdShouldReturnPage() {
        Page<CollectionArt> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(collectionArtRepository.findByArtId(eq(1L), any()))
                .thenReturn(page);

        Page<CollectionArtDto> result
                = collectionArtService.getCollectionsByArtId(
                        1L,
                        PageRequest.of(0, 10)
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCollectionArtDtosByCollectionIdShouldReturnPage() {
        Page<CollectionArt> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(collectionArtRepository.findByCollectionId(eq(1L), any()))
                .thenReturn(page);

        Page<CollectionArtDto> result
                = collectionArtService.getCollectionArtDtosByCollectionId(
                        1L,
                        PageRequest.of(0, 10)
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllArtsByCollectionIdShouldReturnEmptyList() {
        when(collectionArtRepository.findByCollectionId(1L))
                .thenReturn(Collections.emptyList());

        List<?> result
                = collectionArtService.getAllArtsByCollectionId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCollectionsByArtIdShouldReturnList() {
        when(collectionArtRepository.findByArtId(1L))
                .thenReturn(List.of(collectionArt));

        when(collectionArtMapper.toDtos(List.of(collectionArt)))
                .thenReturn(List.of(collectionArtDto));

        List<CollectionArtDto> result
                = collectionArtService.getCollectionsByArtId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getArtId());
    }

    @Test
    void getCollectionArtDtosByCollectionIdShouldReturnList() {
        when(collectionArtRepository.findByCollectionId(1L))
                .thenReturn(List.of(collectionArt));

        when(collectionArtMapper.toDtos(List.of(collectionArt)))
                .thenReturn(List.of(collectionArtDto));

        List<CollectionArtDto> result
                = collectionArtService.getCollectionArtDtosByCollectionId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCollectionId());
    }

    @Test
    void getArtCountByCollectionIdShouldReturnCount() {
        when(collectionArtRepository.countByCollectionIdAndArtStatus(
                1L, ArtStatus.ACTIVE
        )).thenReturn(5L);

        Long result
                = collectionArtService.getArtCountByCollectionId(1L);

        assertEquals(5L, result);
    }

    @Test
    void existsByCollectionIdAndArtIdShouldReturnTrue() {
        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(true);

        assertTrue(
                collectionArtService.existsByCollectionIdAndArtId(1L, 1L)
        );
    }

    @Test
    void existsByCollectionIdAndArtIdShouldReturnFalse() {
        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 2L
        )).thenReturn(false);

        assertFalse(
                collectionArtService.existsByCollectionIdAndArtId(1L, 2L)
        );
    }

    @Test
    void copyArtToCollectionShouldDelegateToAddArt() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        when(art.getIsPublicFlag()).thenReturn(true);
        when(art.getStatus()).thenReturn(ArtStatus.ACTIVE);
        when(collection.getCoverImageUrl()).thenReturn("existing-cover.jpg");

        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(false);

        when(collectionArtRepository.save(any(CollectionArt.class)))
                .thenReturn(collectionArt);

        when(collectionArtMapper.toDto(collectionArt))
                .thenReturn(collectionArtDto);

        CollectionArtDto result
                = collectionArtService.copyArtToCollection(1L, 1L);

        assertNotNull(result);
        verify(collectionArtRepository).save(any(CollectionArt.class));
    }

    @Test
    void moveArtBetweenCollectionsShouldRejectSameCollection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.moveArtBetweenCollections(
                        1L, 1L, 1L
                )
        );

        verify(collectionArtRepository, never())
                .deleteByCollectionIdAndArtId(anyLong(), anyLong());
    }

    @Test
    void moveArtBetweenCollectionsShouldRejectMissingSourceArt() {
        when(collectionRepository.findById(1L))
                .thenReturn(Optional.of(collection));

        when(collectionRepository.findById(2L))
                .thenReturn(Optional.of(collection));

        when(collectionArtRepository.existsByCollectionIdAndArtId(
                1L, 1L
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> collectionArtService.moveArtBetweenCollections(
                        1L, 1L, 2L
                )
        );

        verify(collectionArtRepository, never())
                .deleteByCollectionIdAndArtId(anyLong(), anyLong());
    }
}
