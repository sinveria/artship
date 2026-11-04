package com.example.artship.features.like.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.like.model.ArtLikes;
import com.example.artship.entities.like.repository.LikeRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.like.dto.LikeDto;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtRepository artRepository;

    @Mock
    private LikedArtService likedArtService;

    @InjectMocks
    private LikeService likeService;

    private User createUser(Long id, String username) {
        User user = mock(User.class);

        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);

        return user;
    }

    private Art createArt(Long id, String title) {
        Art art = mock(Art.class);

        when(art.getId()).thenReturn(id);
        when(art.getTitle()).thenReturn(title);

        return art;
    }

    private ArtLikes createLike() {
        User user = createUser(1L, "user1");
        Art art = createArt(2L, "Artwork");

        return new ArtLikes(user, art);
    }

    @Test
    void addLikeShouldSaveLikeAndSynchronizeLikedCollection() {
        User user = createUser(1L, "user1");
        Art art = createArt(2L, "Artwork");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(artRepository.findById(2L))
                .thenReturn(Optional.of(art));

        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(false);

        when(likeRepository.save(any(ArtLikes.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LikeDto result = likeService.addLike(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(2L, result.getArtId());
        assertEquals("user1", result.getUsername());
        assertEquals("Artwork", result.getArtTitle());

        verify(likeRepository).save(any(ArtLikes.class));
        verify(likedArtService).addArtToLiked(1L, 2L);
    }

    @Test
    void addLikeShouldStillReturnLikeWhenLikedCollectionSynchronizationFails() {
        User user = createUser(1L, "user1");
        Art art = createArt(2L, "Artwork");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(artRepository.findById(2L))
                .thenReturn(Optional.of(art));

        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(false);

        when(likeRepository.save(any(ArtLikes.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("Collection error"))
                .when(likedArtService)
                .addArtToLiked(1L, 2L);

        LikeDto result = likeService.addLike(1L, 2L);

        assertNotNull(result);
        assertEquals(2L, result.getArtId());

        verify(likeRepository).save(any(ArtLikes.class));
    }

    @Test
    void addLikeShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> likeService.addLike(1L, 2L)
        );

        verifyNoInteractions(artRepository);
        verify(likeRepository, never()).save(any());
        verifyNoInteractions(likedArtService);
    }

    @Test
    void addLikeShouldThrowWhenArtDoesNotExist() {
        User user = mock(User.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(artRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> likeService.addLike(1L, 2L)
        );

        verify(likeRepository, never()).save(any());
        verifyNoInteractions(likedArtService);
    }

    @Test
    void addLikeShouldRejectDuplicateLike() {
        User user = mock(User.class);
        Art art = mock(Art.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(artRepository.findById(2L))
                .thenReturn(Optional.of(art));

        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> likeService.addLike(1L, 2L)
        );

        verify(likeRepository, never()).save(any());
        verifyNoInteractions(likedArtService);
    }

    @Test
    void removeLikeShouldDeleteLikeAndSynchronizeCollection() {
        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(true);

        likeService.removeLike(1L, 2L);

        verify(likeRepository).deleteByUserIdAndArtId(1L, 2L);
        verify(likedArtService).removeArtFromLiked(1L, 2L);
    }

    @Test
    void removeLikeShouldDeleteLikeWhenCollectionSynchronizationFails() {
        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(true);

        doThrow(new RuntimeException("Collection error"))
                .when(likedArtService)
                .removeArtFromLiked(1L, 2L);

        assertDoesNotThrow(
                () -> likeService.removeLike(1L, 2L)
        );

        verify(likeRepository).deleteByUserIdAndArtId(1L, 2L);
    }

    @Test
    void removeLikeShouldThrowWhenLikeDoesNotExist() {
        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> likeService.removeLike(1L, 2L)
        );

        verify(likeRepository, never())
                .deleteByUserIdAndArtId(anyLong(), anyLong());

        verifyNoInteractions(likedArtService);
    }

    @Test
    void isLikedShouldReturnTrue() {
        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(true);

        assertTrue(likeService.isLiked(1L, 2L));
    }

    @Test
    void isLikedShouldReturnFalse() {
        when(likeRepository.existsByUserIdAndArtId(1L, 2L))
                .thenReturn(false);

        assertFalse(likeService.isLiked(1L, 2L));
    }

    @Test
    void getLikesByArtIdShouldReturnPage() {
        ArtLikes like = createLike();
        PageRequest pageable = PageRequest.of(0, 10);

        when(likeRepository.findByArtId(2L, pageable))
                .thenReturn(new PageImpl<>(List.of(like)));

        Page<LikeDto> result
                = likeService.getLikesByArtId(2L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getUserId());
        assertEquals(2L, result.getContent().get(0).getArtId());
    }

    @Test
    void getLikesByArtIdShouldReturnEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(likeRepository.findByArtId(2L, pageable))
                .thenReturn(Page.empty(pageable));

        Page<LikeDto> result
                = likeService.getLikesByArtId(2L, pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLikeCountByArtIdShouldReturnCount() {
        when(likeRepository.countByArtId(2L))
                .thenReturn(15L);

        assertEquals(15L, likeService.getLikeCountByArtId(2L));
    }

    @Test
    void getLikeCountByUserIdShouldReturnCount() {
        when(likeRepository.countByUserId(1L))
                .thenReturn(8L);

        assertEquals(8L, likeService.getLikeCountByUserId(1L));
    }

    @Test
    void deleteAllUserLikesShouldDeleteLikesAndLikedCollection() {
        likeService.deleteAllUserLikes(1L);

        verify(likeRepository).deleteByUserId(1L);
        verify(likedArtService).deleteAllLikedArtsByUser(1L);
    }

    @Test
    void deleteAllUserLikesShouldPropagateCollectionDeletionError() {
        doThrow(new RuntimeException("Collection error"))
                .when(likedArtService)
                .deleteAllLikedArtsByUser(1L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> likeService.deleteAllUserLikes(1L)
        );

        assertEquals("Collection error", exception.getMessage());

        verify(likeRepository).deleteByUserId(1L);
        verify(likedArtService).deleteAllLikedArtsByUser(1L);
    }
}
