
package com.example.artship.features.tag.controller;

import com.example.artship.entities.tag.model.Tag;
import com.example.artship.features.tag.dto.TagDto;
import com.example.artship.features.tag.service.TagService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TagService tagService;

    private TagDto createTagDto(String name) {
        return new TagDto(new Tag(name));
    }

    @Test
    void getAllTagsShouldReturnTags() throws Exception {
        when(tagService.getAllTagDtos())
                .thenReturn(List.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("digital-art"));

        verify(tagService).getAllTagDtos();
    }

    @Test
    void getTagByIdShouldReturnTagWhenExists() throws Exception {
        when(tagService.getTagDtoById(1L))
                .thenReturn(Optional.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("digital-art"));

        verify(tagService).getTagDtoById(1L);
    }

    @Test
    void getTagByIdShouldReturnNotFoundWhenTagDoesNotExist()
            throws Exception {
        when(tagService.getTagDtoById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tags/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTagByNameShouldReturnTagWhenExists() throws Exception {
        when(tagService.getTagDtoByName("digital-art"))
                .thenReturn(Optional.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags/name/digital-art"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("digital-art"));
    }

    @Test
    void getTagByNameShouldReturnNotFoundWhenTagDoesNotExist()
            throws Exception {
        when(tagService.getTagDtoByName("unknown"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tags/name/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchTagsShouldReturnMatchingTags() throws Exception {
        when(tagService.searchTagDtos("art"))
                .thenReturn(List.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags/search")
                        .param("name", "art"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("digital-art"));

        verify(tagService).searchTagDtos("art");
    }

    @Test
    void getTagsByArtIdShouldReturnTags() throws Exception {
        when(tagService.getTagDtosByArtId(1L))
                .thenReturn(List.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags/art/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("digital-art"));
    }

    @Test
    void getPopularTagsShouldReturnLimitedTags() throws Exception {
        when(tagService.getPopularTagDtos(5))
                .thenReturn(List.of(
                        createTagDto("digital-art"),
                        createTagDto("painting")
                ));

        mockMvc.perform(get("/api/tags/popular")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(tagService).getPopularTagDtos(5);
    }

    @Test
    void getPopularTagsShouldUseDefaultLimit() throws Exception {
        when(tagService.getPopularTagDtos(10))
                .thenReturn(List.of(createTagDto("digital-art")));

        mockMvc.perform(get("/api/tags/popular"))
                .andExpect(status().isOk());

        verify(tagService).getPopularTagDtos(10);
    }

    @Test
    void createTagShouldReturnCreatedTag() throws Exception {
        Tag tag = new Tag("digital-art");

        when(tagService.createTag("digital-art"))
                .thenReturn(tag);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "digital-art"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("digital-art"));

        verify(tagService).createTag("digital-art");
    }

    @Test
    void createTagShouldReturnBadRequestWhenNameIsBlank()
            throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tagService);
    }

    @Test
    void createTagShouldReturnBadRequestWhenNameIsTooLong()
            throws Exception {
        String longName = "a".repeat(51);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s"
                                }
                                """.formatted(longName)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tagService);
    }

    @Test
    void updateTagShouldReturnUpdatedTag() throws Exception {
        Tag tag = new Tag("new-name");

        when(tagService.updateTag(1L, "new-name"))
                .thenReturn(tag);

        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "new-name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new-name"));

        verify(tagService).updateTag(1L, "new-name");
    }

    @Test
    void updateTagShouldReturnBadRequestWhenNameIsBlank()
            throws Exception {
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tagService);
    }

    @Test
    void deleteTagShouldReturnNoContent() throws Exception {
        doNothing().when(tagService).deleteTag(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());

        verify(tagService).deleteTag(1L);
    }

    @Test
    void deleteTagShouldReturnNotFoundWhenTagDoesNotExist()
            throws Exception {
        doThrow(new IllegalArgumentException("Тег не найден"))
                .when(tagService).deleteTag(999L);

        mockMvc.perform(delete("/api/tags/999"))
                .andExpect(status().isNotFound());
    }
}