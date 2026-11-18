package com.example.artship.features.sociallink.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.artship.features.sociallink.model.SocialPlatform;
import com.example.artship.features.sociallink.service.SocialLinkService;

@ExtendWith(MockitoExtension.class)
class SocialLinkControllerTest {

    @Mock
    private SocialLinkService socialLinkService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SocialLinkController controller
                = new SocialLinkController(socialLinkService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getUserSocialLinksShouldReturnLinks() throws Exception {
        when(socialLinkService.getUserSocialLinks(1L, false))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/social-links/user/1")
                .param("onlyVisible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.socialLinks").isArray())
                .andExpect(jsonPath("$.count").value(0));

        verify(socialLinkService).getUserSocialLinks(1L, false);
    }

    @Test
    void addSocialLinkShouldReturnCreated() throws Exception {
        when(socialLinkService.addSocialLink(
                eq(1L),
                any()
        )).thenReturn(null);

        mockMvc.perform(post("/api/social-links")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "platform": "TELEGRAM",
                                  "url": "https://t.me/example",
                                  "visible": true,
                                  "displayOrder": 0
                                }
                                """))
                .andExpect(status().isCreated());

        verify(socialLinkService).addSocialLink(eq(1L), any());
    }

    @Test
    void updateSocialLinkShouldReturnUpdatedLink() throws Exception {
        when(socialLinkService.updateSocialLink(
                eq(1L),
                eq(10L),
                any()
        )).thenReturn(null);

        mockMvc.perform(put("/api/social-links/10")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "platform": "VKONTAKTE",
                                  "url": "https://vk.com/example",
                                  "visible": true,
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isOk());

        verify(socialLinkService)
                .updateSocialLink(eq(1L), eq(10L), any());
    }

    @Test
    void deleteSocialLinkShouldReturnSuccessMessage() throws Exception {
        doNothing().when(socialLinkService)
                .deleteSocialLink(1L, 10L);

        mockMvc.perform(delete("/api/social-links/10")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Social link deleted successfully"));

        verify(socialLinkService).deleteSocialLink(1L, 10L);
    }

    @Test
    void updateDisplayOrderShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/social-links/order")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[10, 20, 30]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Display order updated successfully"));

        verify(socialLinkService)
                .updateDisplayOrder(1L, List.of(10L, 20L, 30L));
    }

    @Test
    void updateAllSocialLinksShouldReturnUpdatedLinks() throws Exception {
        when(socialLinkService.updateAllSocialLinks(
                eq(1L),
                anyList()
        )).thenReturn(List.of());

        mockMvc.perform(put("/api/social-links/batch")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                [
                                  {
                                    "platform": "TELEGRAM",
                                    "url": "https://t.me/example",
                                    "visible": true,
                                    "displayOrder": 0
                                  },
                                  {
                                    "platform": "VKONTAKTE",
                                    "url": "https://vk.com/example",
                                    "visible": false,
                                    "displayOrder": 1
                                  }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.socialLinks").isArray())
                .andExpect(jsonPath("$.message")
                        .value("All social links updated successfully"));

        verify(socialLinkService)
                .updateAllSocialLinks(eq(1L), anyList());
    }

    @Test
    void getAvailablePlatformsShouldReturnAllPlatforms() throws Exception {
        mockMvc.perform(get("/api/social-links/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platforms").isArray())
                .andExpect(jsonPath("$.platforms.length()").value(
                        SocialPlatform.values().length
                ))
                .andExpect(jsonPath("$.platforms[0].name").exists())
                .andExpect(jsonPath("$.platforms[0].displayName").exists())
                .andExpect(jsonPath("$.platforms[0].baseUrl").exists());
    }

    @Test
    void validateUrlShouldReturnValidationResult() throws Exception {
        when(socialLinkService.validateUrl(
                "https://t.me/example",
                SocialPlatform.TELEGRAM
        )).thenReturn(true);

        mockMvc.perform(post("/api/social-links/validate")
                .param("url", "https://t.me/example")
                .param("platform", "TELEGRAM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url")
                        .value("https://t.me/example"))
                .andExpect(jsonPath("$.platform").value("TELEGRAM"))
                .andExpect(jsonPath("$.valid").value(true));

        verify(socialLinkService).validateUrl(
                "https://t.me/example",
                SocialPlatform.TELEGRAM
        );
    }

    @Test
    void addSocialLinkShouldReturnBadRequestWhenServiceThrowsException()
            throws Exception {

        when(socialLinkService.addSocialLink(
                eq(1L),
                any()
        )).thenThrow(new IllegalArgumentException("Duplicate platform"));

        mockMvc.perform(post("/api/social-links")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "platform": "TELEGRAM",
                                  "url": "https://t.me/example",
                                  "visible": true,
                                  "displayOrder": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Duplicate platform"));
    }

    @Test
    void updateSocialLinkShouldReturnBadRequestWhenServiceThrowsException()
            throws Exception {

        when(socialLinkService.updateSocialLink(
                eq(1L),
                eq(10L),
                any()
        )).thenThrow(new IllegalArgumentException("Link not found"));

        mockMvc.perform(put("/api/social-links/10")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "platform": "TELEGRAM",
                                  "url": "https://t.me/example",
                                  "visible": true,
                                  "displayOrder": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Link not found"));
    }
}
