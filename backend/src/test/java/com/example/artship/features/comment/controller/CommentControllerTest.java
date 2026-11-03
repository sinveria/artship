package com.example.artship.features.comment.controller;

import com.example.artship.app.exception.GlobalExceptionHandler;
import com.example.artship.features.comment.dto.CommentDto;
import com.example.artship.features.comment.service.CommentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Nice art");
        commentDto.setArtId(1L);
        commentDto.setUserId(1L);
    }

    @Test
    void getCommentByIdShouldReturnComment() throws Exception {
        when(commentService.getCommentById(1L)).thenReturn(commentDto);

        mockMvc.perform(get("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Nice art"));
    }

    @Test
    void getCommentByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(commentService.getCommentById(999L))
                .thenThrow(new IllegalArgumentException("Комментарий не найден"));

        mockMvc.perform(get("/api/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommentsByArtIdShouldReturnList() throws Exception {
        when(commentService.getCommentsByArtId(1L))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/api/comments/art/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getRootCommentsByArtIdShouldReturnList() throws Exception {
        when(commentService.getRootCommentsByArtId(1L))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/api/comments/art/1/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Nice art"));
    }

    @Test
    void getRepliesShouldReturnList() throws Exception {
        when(commentService.getReplies(1L))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/api/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCommentsByUserIdShouldReturnList() throws Exception {
        when(commentService.getCommentsByUserId(1L))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/api/comments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void countCommentsByArtIdShouldReturnCount() throws Exception {
        when(commentService.countCommentsByArtId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/comments/art/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void createCommentShouldReturnCreated() throws Exception {
        when(commentService.createComment("Nice art", 1L, 1L))
                .thenReturn(commentDto);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "Nice art",
                                    "artId": 1,
                                    "userId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCommentShouldRejectBlankText() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "   ",
                                    "artId": 1,
                                    "userId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void createCommentShouldRejectTextLongerThan2000Characters() throws Exception {
        String longText = "a".repeat(2001);

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "%s",
                                    "artId": 1,
                                    "userId": 1
                                }
                                """.formatted(longText)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void createReplyShouldReturnCreated() throws Exception {
        when(commentService.createReply("Reply text", 1L, 1L))
                .thenReturn(commentDto);

        mockMvc.perform(post("/api/comments/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "Reply text",
                                    "parentCommentId": 1,
                                    "userId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Nice art"));
    }

    @Test
    void updateCommentShouldReturnUpdatedComment() throws Exception {
        CommentDto updatedDto = new CommentDto();
        updatedDto.setId(1L);
        updatedDto.setText("Updated");
        updatedDto.setArtId(1L);
        updatedDto.setUserId(1L);

        when(commentService.updateComment(1L, 1L, "Updated"))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "Updated",
                                    "userId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated"));
    }

    @Test
    void updateCommentShouldRejectBlankText() throws Exception {
        mockMvc.perform(put("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "text": "",
                                    "userId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void hideCommentShouldReturnHiddenComment() throws Exception {
        CommentDto hiddenDto = new CommentDto();
        hiddenDto.setId(1L);
        hiddenDto.setText("Nice art");
        hiddenDto.setArtId(1L);
        hiddenDto.setUserId(1L);
        hiddenDto.setHidden(true);

        when(commentService.hideComment(1L)).thenReturn(hiddenDto);

        mockMvc.perform(patch("/api/comments/1/hide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hidden").value(true));
    }

    @Test
    void deleteCommentShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/comments/1")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L, 1L);
    }

    @Test
    void deleteCommentShouldReturnNotFoundWhenMissing() throws Exception {
        doThrow(new IllegalArgumentException("Комментарий не найден"))
                .when(commentService).deleteComment(999L, 1L);

        mockMvc.perform(delete("/api/comments/999")
                        .param("userId", "1"))
                .andExpect(status().isNotFound());
    }
}