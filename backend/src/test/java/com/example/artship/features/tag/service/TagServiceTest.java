package com.example.artship.features.tag.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.tag.model.Tag;
import com.example.artship.entities.tag.repository.TagRepository;
import com.example.artship.features.tag.dto.TagDto;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArtRepository artRepository;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private Art art;

    @BeforeEach
    void setUp() {
        tag = new Tag("digital-art");
        tag.setArts(new ArrayList<>());

        art = new Art();
        art.setTags(new ArrayList<>());
    }

    @Test
    void getTagByIdShouldReturnTagWhenExists() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Optional<Tag> result = tagService.getTagById(1L);

        assertTrue(result.isPresent());
        assertEquals("digital-art", result.get().getName());
        verify(tagRepository).findById(1L);
    }

    @Test
    void getTagByNameShouldReturnTagWhenExists() {
        when(tagRepository.findByName("digital-art"))
                .thenReturn(Optional.of(tag));

        Optional<Tag> result = tagService.getTagByName("digital-art");

        assertTrue(result.isPresent());
        assertEquals("digital-art", result.get().getName());
    }

    @Test
    void getAllTagsShouldReturnTagsSortedByName() {
        Tag secondTag = new Tag("watercolor");
        Tag firstTag = new Tag("abstract");

        when(tagRepository.findAll())
                .thenReturn(List.of(secondTag, firstTag));

        List<Tag> result = tagService.getAllTags();

        assertEquals(2, result.size());
        assertEquals("abstract", result.get(0).getName());
        assertEquals("watercolor", result.get(1).getName());
    }

    @Test
    void searchTagsShouldReturnMatchingTags() {
        when(tagRepository.findByNameContainingIgnoreCase("art"))
                .thenReturn(List.of(tag));

        List<Tag> result = tagService.searchTags("art");

        assertEquals(1, result.size());
        assertEquals(tag, result.get(0));
    }

    @Test
    void getTagsByArtIdShouldReturnArtTags() {
        art.getTags().add(tag);

        when(artRepository.findById(1L)).thenReturn(Optional.of(art));

        List<Tag> result = tagService.getTagsByArtId(1L);

        assertEquals(1, result.size());
        assertEquals(tag, result.get(0));
    }

    @Test
    void getTagsByArtIdShouldThrowWhenArtDoesNotExist() {
        when(artRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tagService.getTagsByArtId(1L)
        );
    }

    @Test
    void getPopularTagsShouldReturnTagsOrderedByArtCount() {
        Tag popularTag = new Tag("popular");
        popularTag.setArts(new ArrayList<>());

        Tag unpopularTag = new Tag("unpopular");
        unpopularTag.setArts(new ArrayList<>());

        popularTag.getArts().add(new Art());
        popularTag.getArts().add(new Art());
        unpopularTag.getArts().add(new Art());

        when(tagRepository.findAll())
                .thenReturn(List.of(unpopularTag, popularTag));

        List<Tag> result = tagService.getPopularTags(10);

        assertEquals(2, result.size());
        assertEquals("popular", result.get(0).getName());
        assertEquals("unpopular", result.get(1).getName());
    }

    @Test
    void getPopularTagsShouldRespectLimit() {
        Tag first = new Tag("first");
        first.setArts(new ArrayList<>());

        Tag second = new Tag("second");
        second.setArts(new ArrayList<>());

        when(tagRepository.findAll()).thenReturn(List.of(first, second));

        List<Tag> result = tagService.getPopularTags(1);

        assertEquals(1, result.size());
    }

    @Test
    void getPopularTagsShouldReturnEmptyListForNonPositiveLimit() {
        List<Tag> result = tagService.getPopularTags(0);

        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void createTagShouldReturnExistingTag() {
        when(tagRepository.findByName("digital-art"))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.createTag("digital-art");

        assertEquals(tag, result);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTagShouldSaveNewTag() {
        when(tagRepository.findByName("digital-art"))
                .thenReturn(Optional.empty());

        when(tagRepository.save(any(Tag.class)))
                .thenReturn(tag);

        Tag result = tagService.createTag("digital-art");

        assertEquals(tag, result);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void updateTagShouldChangeName() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);

        Tag result = tagService.updateTag(1L, "new-name");

        assertEquals("new-name", result.getName());
        verify(tagRepository).save(tag);
    }

    @Test
    void updateTagShouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(1L, "new-name")
        );
    }

    @Test
    void updateTagShouldThrowWhenNewNameAlreadyExists() {
        Tag anotherTag = new Tag("another-tag");

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByName("another-tag")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> tagService.updateTag(1L, "another-tag")
        );

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void deleteTagShouldRemoveRelationsAndDeleteTag() {
        art.getTags().add(tag);
        tag.getArts().add(art);

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        tagService.deleteTag(1L);

        assertFalse(art.getTags().contains(tag));
        verify(artRepository).saveAll(tag.getArts());
        verify(tagRepository).delete(tag);
    }

    @Test
    void deleteTagShouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tagService.deleteTag(1L)
        );

        verify(tagRepository, never()).delete(any(Tag.class));
    }

    @Test
    void findOrCreateTagShouldReturnExistingTag() {
        when(tagRepository.findByName("digital-art"))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.findOrCreateTag("digital-art");

        assertEquals(tag, result);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTagsShouldCreateOrFindAllTags() {
        Tag secondTag = new Tag("painting");

        when(tagRepository.findByName("digital-art"))
                .thenReturn(Optional.of(tag));

        when(tagRepository.findByName("painting"))
                .thenReturn(Optional.empty());

        when(tagRepository.save(any(Tag.class)))
                .thenReturn(secondTag);

        List<Tag> result = tagService.createTags(
                List.of("digital-art", "painting")
        );

        assertEquals(2, result.size());
        assertEquals(tag, result.get(0));
        assertEquals(secondTag, result.get(1));
    }

    @Test
    void tagExistsShouldReturnTrueWhenTagExists() {
        when(tagRepository.existsByName("digital-art")).thenReturn(true);

        assertTrue(tagService.tagExists("digital-art"));
    }

    @Test
    void getArtCountByTagIdShouldReturnNumberOfArts() {
        tag.getArts().add(new Art());
        tag.getArts().add(new Art());

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        Long result = tagService.getArtCountByTagId(1L);

        assertEquals(2L, result);
    }

    @Test
    void getArtCountByTagIdShouldThrowWhenTagDoesNotExist() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tagService.getArtCountByTagId(1L)
        );
    }

    @Test
    void getAllTagDtosShouldReturnDtos() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<TagDto> result = tagService.getAllTagDtos();

        assertEquals(1, result.size());
        assertEquals("digital-art", result.get(0).getName());
    }
}