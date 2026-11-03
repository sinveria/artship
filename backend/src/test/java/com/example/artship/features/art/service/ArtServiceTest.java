package com.example.artship.features.art.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.user.dto.UserDto;

@ExtendWith(MockitoExtension.class)
class ArtServiceTest {

    @Mock
    private ArtRepository artRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArtService artService;

    @Test
    void getArtByIdShouldReturnArt() {
        User author = new User();
        Art art = new Art("Test Art", "image.png", author);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        Art result = artService.getArtById(1L);

        assertNotNull(result);
        assertEquals("Test Art", result.getTitle());

        verify(artRepository).findById(1L);
    }

    @Test
    void getArtByIdShouldThrowExceptionWhenNotFound() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> artService.getArtById(1L)
        );
    }

    @Test
    void getPublicArtsShouldReturnPage() {
        User author = new User();
        Art art = new Art("Public Art", "image.png", author);

        Page<Art> expectedPage = new PageImpl<>(List.of(art));
        PageRequest pageable = PageRequest.of(0, 20);

        when(artRepository.findByIsPublicFlagTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(expectedPage);

        Page<Art> result = artService.getPublicArts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Public Art", result.getContent().get(0).getTitle());
    }

    @Test
    void getArtsByAuthorShouldReturnPage() {
        User author = new User();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(author));

        Page<Art> expectedPage = new PageImpl<>(
                List.of(new Art("Author Art", "image.png", author))
        );

        PageRequest pageable = PageRequest.of(0, 20);

        when(artRepository.findByAuthor(author, pageable))
                .thenReturn(expectedPage);

        Page<Art> result = artService.getArtsByAuthor(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findById(1L);
    }

    @Test
    void getArtsByAuthorShouldThrowExceptionWhenAuthorNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> artService.getArtsByAuthor(
                        1L,
                        PageRequest.of(0, 20)
                )
        );
    }

    @Test
    void createArtShouldSaveArt() {
        User author = new User();
        UserDto authorDto = new UserDto();
        authorDto.setId(1L);

        ArtDto artDto = new ArtDto();
        artDto.setTitle("New Art");
        artDto.setDescription("Description");
        artDto.setImage("image.png");
        artDto.setProjectDataUrl("project.json");
        artDto.setAuthor(authorDto);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(author));

        when(artRepository.save(any(Art.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Art result = artService.createArt(artDto);

        assertNotNull(result);
        assertEquals("New Art", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(author, result.getAuthor());
        assertEquals(ArtStatus.ACTIVE, result.getStatus());

        verify(artRepository).save(any(Art.class));
    }

    @Test
    void createArtShouldThrowExceptionWhenAuthorIsMissing() {
        ArtDto artDto = new ArtDto();

        assertThrows(
                IllegalArgumentException.class,
                () -> artService.createArt(artDto)
        );

        verify(artRepository, never()).save(any(Art.class));
    }

    @Test
    void deleteArtShouldDeleteExistingArt() {
        User author = new User();
        Art art = new Art("Art", "image.png", author);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        artService.deleteArt(1L);

        verify(artRepository).delete(art);
    }

    @Test
    void countByStatusShouldReturnCount() {
        when(artRepository.countByStatus(ArtStatus.ACTIVE))
                .thenReturn(5L);

        long result = artService.countByStatus(ArtStatus.ACTIVE);

        assertEquals(5L, result);
    }
}