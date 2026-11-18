package com.example.artship.features.arttag.controller;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.tag.model.Tag;
import com.example.artship.features.art.mapper.ArtMapper;
import com.example.artship.features.art.service.ArtService;
import com.example.artship.features.arttag.dto.ArtTagDto;
import com.example.artship.features.arttag.service.ArtTagService;
import com.example.artship.features.tag.dto.TagDto;
import com.example.artship.features.tag.service.TagService;

@WebMvcTest(ArtTagController.class)
@Import(ArtMapper.class)
class ArtTagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtTagService artTagService;

    @MockBean
    private TagService tagService;

    @MockBean
    private ArtService artService;

    @Test
    void addTagToArtShouldReturnCreated() throws Exception {
        when(artTagService.createArtTag(1L, 2L))
                .thenReturn(new ArtTagDto(
                        1L,
                        "Test art",
                        2L,
                        "digital-art"
                ));

        mockMvc.perform(post("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artId").value(1))
                .andExpect(jsonPath("$.tagId").value(2))
                .andExpect(jsonPath("$.tagName").value("digital-art"));

        verify(artTagService).createArtTag(1L, 2L);
    }

    @Test
    void addTagToArtShouldReturnBadRequestWhenRelationCannotBeCreated()
            throws Exception {
        when(artTagService.createArtTag(1L, 2L))
                .thenThrow(new IllegalArgumentException(
                        "Этот тег уже добавлен к работе"
                ));

        mockMvc.perform(post("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Этот тег уже добавлен к работе"));
    }

    @Test
    void removeTagFromArtShouldReturnNoContent() throws Exception {
        doNothing().when(artTagService).removeTagFromArt(1L, 2L);

        mockMvc.perform(delete("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isNoContent());

        verify(artTagService).removeTagFromArt(1L, 2L);
    }

    @Test
    void removeTagFromArtShouldReturnNotFoundWhenRelationDoesNotExist()
            throws Exception {
        doThrow(new IllegalArgumentException("Тег не найден"))
                .when(artTagService).removeTagFromArt(1L, 2L);

        mockMvc.perform(delete("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Тег не найден"));
    }

    @Test
    void checkTagArtRelationShouldReturnTrue() throws Exception {
        when(artTagService.existsByArtIdAndTagId(1L, 2L))
                .thenReturn(true);

        mockMvc.perform(get("/api/art-tags/art/1/tag/2/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getArtTagRelationShouldReturnRelation() throws Exception {
        when(artTagService.getArtTagDtoByArtIdAndTagId(1L, 2L))
                .thenReturn(Optional.of(new ArtTagDto(
                        1L,
                        "Test art",
                        2L,
                        "digital-art"
                )));

        mockMvc.perform(get("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artTitle").value("Test art"))
                .andExpect(jsonPath("$.tagName").value("digital-art"));
    }

    @Test
    void getArtTagRelationShouldReturnNotFoundWhenRelationDoesNotExist()
            throws Exception {
        when(artTagService.getArtTagDtoByArtIdAndTagId(1L, 2L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/art-tags/art/1/tag/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getArtTagsByArtShouldReturnPage() throws Exception {
        when(artTagService.getArtTagDtosByArtId(
                1L,
                PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(
                List.of(new ArtTagDto(
                        1L,
                        "Test art",
                        2L,
                        "digital-art"
                )),
                PageRequest.of(0, 20),
                1
        ));

        mockMvc.perform(get("/api/art-tags/art/1/art-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tagName")
                        .value("digital-art"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getArtTagsByTagShouldReturnPage() throws Exception {
        when(artTagService.getArtTagDtosByTagId(
                2L,
                PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(
                List.of(new ArtTagDto(
                        1L,
                        "Test art",
                        2L,
                        "digital-art"
                )),
                PageRequest.of(0, 20),
                1
        ));

        mockMvc.perform(get("/api/art-tags/tag/2/art-tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].artTitle")
                        .value("Test art"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getArtCountByTagShouldReturnCount() throws Exception {
        when(artTagService.getArtCountByTagId(2L))
                .thenReturn(5L);

        mockMvc.perform(get("/api/art-tags/tag/2/arts/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    void getTagCountByArtShouldReturnCount() throws Exception {
        when(artTagService.getTagCountByArtId(1L))
                .thenReturn(3L);

        mockMvc.perform(get("/api/art-tags/art/1/tags/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    void removeAllTagsFromArtShouldReturnNoContent() throws Exception {
        doNothing().when(artTagService).removeAllTagsFromArt(1L);

        mockMvc.perform(delete("/api/art-tags/art/1/tags"))
                .andExpect(status().isNoContent());

        verify(artTagService).removeAllTagsFromArt(1L);
    }

    @Test
    void removeAllTagsFromArtShouldReturnNotFoundWhenArtDoesNotExist()
            throws Exception {
        doThrow(new IllegalArgumentException("Работа не найдена"))
                .when(artTagService).removeAllTagsFromArt(1L);

        mockMvc.perform(delete("/api/art-tags/art/1/tags"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTagsBatchToArtShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/art-tags/art/1/tags/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "tagNames": [
                                        "digital-art",
                                        "painting"
                                    ]
                                }
                                """))
                .andExpect(status().isOk());

        verify(artTagService).addTagsToArt(
                1L,
                List.of("digital-art", "painting")
        );
    }

    @Test
    void addTagsBatchToArtShouldReturnBadRequestWhenListIsEmpty()
            throws Exception {
        mockMvc.perform(post("/api/art-tags/art/1/tags/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                    "tagNames": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(artTagService);
    }

    @Test
    void getTagsByArtShouldReturnTags() throws Exception {
        TagDto tagDto = new TagDto(new Tag("digital-art"));

        when(tagService.getTagDtosByArtId(1L))
                .thenReturn(List.of(tagDto));

        mockMvc.perform(get("/api/art-tags/art/1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("digital-art"));

        verify(tagService).getTagDtosByArtId(1L);
    }

    @Test
    void getArtsByTagShouldReturnArts() throws Exception {
        Art art = new Art();
        art.setTitle("Test art");

        when(artService.getArtsByTag(
                2L,
                PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(
                List.of(art),
                PageRequest.of(0, 20),
                1
        ));

        mockMvc.perform(get("/api/art-tags/tag/2/arts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Test art"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
