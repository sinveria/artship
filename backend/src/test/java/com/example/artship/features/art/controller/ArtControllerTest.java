package com.example.artship.features.art.controller;

import com.example.artship.entities.art.model.Art;
import com.example.artship.features.art.mapper.ArtMapper;
import com.example.artship.features.art.service.ArtService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtController.class)
@Import(ArtMapper.class)
class ArtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtService artService;

    @Test
    void getArtByIdShouldReturnNotFoundWhenArtDoesNotExist()
            throws Exception {

        when(artService.getArtById(1L))
                .thenThrow(new IllegalArgumentException("Арт не найден"));

        mockMvc.perform(get("/api/arts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteArtShouldReturnNoContent() throws Exception {

        doNothing().when(artService).deleteArt(1L);

        mockMvc.perform(delete("/api/arts/1"))
                .andExpect(status().isNoContent());

        verify(artService).deleteArt(1L);
    }
}