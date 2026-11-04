package com.example.artship.features.like.service;

import java.util.List;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.artship.entities.collection.model.Collection;
import com.example.artship.entities.collection.repository.CollectionRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.collection.service.CollectionArtService;

@ExtendWith(MockitoExtension.class)
class LikedArtServiceTest {

    @Mock
    private CollectionArtService collectionArtService;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikedArtService likedArtService;

    private Collection createCollection(Long id) {
        Collection collection = mock(Collection.class);
        when(collection.getId()).thenReturn(id);
        return collection;
    }

    @Test
    void addArtToLikedShouldAddArtToExistingCollection() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(false);

        likedArtService.addArtToLiked(1L, 20L);

        verify(collectionArtService).addArtToCollection(10L, 20L);
    }

    @Test
    void addArtToLikedShouldCreateCollectionWhenItDoesNotExist() {
        User user = mock(User.class);
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(collectionRepository.save(any(Collection.class)))
                .thenReturn(collection);

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(false);

        likedArtService.addArtToLiked(1L, 20L);

        verify(userRepository).findById(1L);
        verify(collectionRepository).save(any(Collection.class));
        verify(collectionArtService).addArtToCollection(10L, 20L);
    }

    @Test
    void addArtToLikedShouldThrowWhenUserDoesNotExist() {
        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> likedArtService.addArtToLiked(1L, 20L)
        );

        verify(collectionRepository, never()).save(any());
        verify(collectionArtService, never())
                .addArtToCollection(anyLong(), anyLong());
    }

    @Test
    void addArtToLikedShouldRejectDuplicateArt() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> likedArtService.addArtToLiked(1L, 20L)
        );

        verify(collectionArtService, never())
                .addArtToCollection(anyLong(), anyLong());
    }

    @Test
    void removeArtFromLikedShouldRemoveArt() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(true);

        likedArtService.removeArtFromLiked(1L, 20L);

        verify(collectionArtService).removeArtFromCollection(10L, 20L);
    }

    @Test
    void removeArtFromLikedShouldThrowWhenCollectionDoesNotExist() {
        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> likedArtService.removeArtFromLiked(1L, 20L)
        );

        verify(collectionArtService, never())
                .removeArtFromCollection(anyLong(), anyLong());
    }

    @Test
    void removeArtFromLikedShouldThrowWhenArtDoesNotExistInCollection() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> likedArtService.removeArtFromLiked(1L, 20L)
        );

        verify(collectionArtService, never())
                .removeArtFromCollection(anyLong(), anyLong());
    }

    @Test
    void isArtLikedByUserShouldReturnTrue() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(true);

        assertTrue(likedArtService.isArtLikedByUser(1L, 20L));
    }

    @Test
    void isArtLikedByUserShouldReturnFalseWhenCollectionDoesNotExist() {
        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        assertFalse(likedArtService.isArtLikedByUser(1L, 20L));

        verifyNoInteractions(collectionArtService);
    }

    @Test
    void isArtLikedByUserShouldReturnFalseWhenArtIsNotInCollection() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.existsByCollectionIdAndArtId(10L, 20L))
                .thenReturn(false);

        assertFalse(likedArtService.isArtLikedByUser(1L, 20L));
    }

    @Test
    void getLikedArtsByUserShouldReturnArts() {
        Collection collection = createCollection(10L);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ArtDto> arts = new PageImpl<>(List.of(mock(ArtDto.class)));

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.getArtsByCollectionId(10L, pageable))
                .thenReturn(arts);

        Page<ArtDto> result
                = likedArtService.getLikedArtsByUser(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(collectionArtService).getArtsByCollectionId(10L, pageable);
    }

    @Test
    void getLikedArtsByUserShouldReturnEmptyPageWhenCollectionDoesNotExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        Page<ArtDto> result
                = likedArtService.getLikedArtsByUser(1L, pageable);

        assertTrue(result.isEmpty());
        verifyNoInteractions(collectionArtService);
    }

    @Test
    void getLikedArtsCountShouldReturnCount() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        when(collectionArtService.getArtCountByCollectionId(10L))
                .thenReturn(5L);

        assertEquals(5L, likedArtService.getLikedArtsCount(1L));
    }

    @Test
    void getLikedArtsCountShouldReturnZeroWhenCollectionDoesNotExist() {
        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        assertEquals(0L, likedArtService.getLikedArtsCount(1L));

        verifyNoInteractions(collectionArtService);
    }

    @Test
    void deleteAllLikedArtsByUserShouldRemoveArtsAndCollection() {
        Collection collection = createCollection(10L);

        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.of(collection));

        likedArtService.deleteAllLikedArtsByUser(1L);

        verify(collectionArtService).removeAllArtsFromCollection(10L);
        verify(collectionRepository).delete(collection);
    }

    @Test
    void deleteAllLikedArtsByUserShouldDoNothingWhenCollectionDoesNotExist() {
        when(collectionRepository.findByUserIdAndTitle(
                1L,
                "Понравившиеся"
        )).thenReturn(Optional.empty());

        likedArtService.deleteAllLikedArtsByUser(1L);

        verify(collectionRepository, never()).delete(any());
        verifyNoInteractions(collectionArtService);
    }
}
