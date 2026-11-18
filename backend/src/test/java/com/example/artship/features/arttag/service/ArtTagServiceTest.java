package com.example.artship.features.arttag.service;

import java.util.ArrayList;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.tag.model.Tag;
import com.example.artship.entities.tag.repository.TagRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.features.arttag.dto.ArtTagDto;
import com.example.artship.features.tag.service.TagService;

@ExtendWith(MockitoExtension.class)
class ArtTagServiceTest {

    @Mock
    private ArtRepository artRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ArtTagService artTagService;

    private Art art;
    private Tag tag;

    @BeforeEach
    void setUp() {
        art = new Art("Test art", "image.png", new User());
        tag = new Tag("digital-art");

        // В unit-тестах Hibernate не назначает идентификаторы автоматически.
        ReflectionTestUtils.setField(art, "id", 1L);
        ReflectionTestUtils.setField(tag, "id", 2L);

        art.setTags(new ArrayList<>());
        tag.setArts(new ArrayList<>());
    }

    @Test
    void createArtTagShouldAddTagToArt() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));
        when(artRepository.save(art)).thenReturn(art);

        ArtTagDto result = artTagService.createArtTag(1L, 2L);

        assertNotNull(result);
        assertEquals("Test art", result.getArtTitle());
        assertEquals("digital-art", result.getTagName());
        assertTrue(art.getTags().contains(tag));
        assertTrue(tag.getArts().contains(art));

        verify(artRepository).save(art);
    }

    @Test
    void createArtTagShouldThrowWhenArtNotFound() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> artTagService.createArtTag(1L, 2L)
        );

        verify(artRepository, never()).save(any(Art.class));
    }

    @Test
    void createArtTagShouldThrowWhenTagNotFound() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> artTagService.createArtTag(1L, 2L)
        );

        verify(artRepository, never()).save(any(Art.class));
    }

    @Test
    void createArtTagShouldThrowWhenRelationAlreadyExists() {
        art.getTags().add(tag);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));

        assertThrows(
                IllegalArgumentException.class,
                () -> artTagService.createArtTag(1L, 2L)
        );

        verify(artRepository, never()).save(any(Art.class));
    }

    @Test
    void getArtTagDtoByArtIdAndTagIdShouldReturnRelation() {
        art.getTags().add(tag);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        Optional<ArtTagDto> result
                = artTagService.getArtTagDtoByArtIdAndTagId(1L, 2L);

        assertTrue(result.isPresent());
        assertEquals("digital-art", result.get().getTagName());
    }

    @Test
    void getArtTagDtoByArtIdAndTagIdShouldReturnEmptyWhenRelationMissing() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        Optional<ArtTagDto> result
                = artTagService.getArtTagDtoByArtIdAndTagId(1L, 2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void removeTagFromArtShouldRemoveRelation() {
        art.getTags().add(tag);
        tag.getArts().add(art);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));

        artTagService.removeTagFromArt(1L, 2L);

        assertFalse(art.getTags().contains(tag));
        assertFalse(tag.getArts().contains(art));

        verify(artRepository).save(art);
    }

    @Test
    void removeTagFromArtShouldThrowWhenRelationMissing() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));

        assertThrows(
                IllegalArgumentException.class,
                () -> artTagService.removeTagFromArt(1L, 2L)
        );

        verify(artRepository, never()).save(any(Art.class));
    }

    @Test
    void existsByArtIdAndTagIdShouldReturnTrue() {
        art.getTags().add(tag);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        assertTrue(
                artTagService.existsByArtIdAndTagId(1L, 2L)
        );
    }

    @Test
    void existsByArtIdAndTagIdShouldReturnFalseWhenArtNotFound() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertFalse(
                artTagService.existsByArtIdAndTagId(1L, 2L)
        );
    }

    @Test
    void getArtTagDtosByArtIdShouldReturnPage() {
        art.getTags().add(tag);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        Page<ArtTagDto> result
                = artTagService.getArtTagDtosByArtId(
                        1L,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "digital-art",
                result.getContent().get(0).getTagName()
        );
    }

    @Test
    void getArtTagDtosByTagIdShouldReturnPage() {
        tag.getArts().add(art);

        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));

        Page<ArtTagDto> result
                = artTagService.getArtTagDtosByTagId(
                        2L,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Test art",
                result.getContent().get(0).getArtTitle()
        );
    }

    @Test
    void getArtCountByTagIdShouldReturnNumberOfArts() {
        Art secondArt = new Art("Second art", "second.png", new User());
        ReflectionTestUtils.setField(secondArt, "id", 3L);

        tag.getArts().add(art);
        tag.getArts().add(secondArt);

        when(tagRepository.findById(2L))
                .thenReturn(Optional.of(tag));

        assertEquals(
                2L,
                artTagService.getArtCountByTagId(2L)
        );
    }

    @Test
    void getTagCountByArtIdShouldReturnNumberOfTags() {
        art.getTags().add(tag);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        assertEquals(
                1L,
                artTagService.getTagCountByArtId(1L)
        );
    }

    @Test
    void removeAllTagsFromArtShouldClearRelations() {
        art.getTags().add(tag);
        tag.getArts().add(art);

        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        artTagService.removeAllTagsFromArt(1L);

        assertTrue(art.getTags().isEmpty());
        assertTrue(tag.getArts().isEmpty());

        verify(artRepository).save(art);
    }

    @Test
    void addTagsToArtShouldAddNewTags() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));
        when(tagService.findOrCreateTag("digital-art"))
                .thenReturn(tag);

        artTagService.addTagsToArt(
                1L,
                List.of("digital-art")
        );

        assertTrue(art.getTags().contains(tag));
        assertTrue(tag.getArts().contains(art));

        verify(artRepository).save(art);
    }

    @Test
    void addTagsToArtShouldThrowWhenTagNamesAreEmpty() {
        when(artRepository.findById(1L))
                .thenReturn(Optional.of(art));

        assertThrows(
                IllegalArgumentException.class,
                () -> artTagService.addTagsToArt(1L, List.of())
        );

        verify(artRepository, never()).save(any(Art.class));
    }
}
