package com.example.artship.features.sociallink.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.features.sociallink.model.SocialPlatform;
import com.example.artship.features.sociallink.request.SocialLinkRequest;
import com.example.artship.features.sociallink.response.SocialLinkResponse;
import com.example.artship.features.sociallink.service.SocialLinkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/social-links")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSocialLinks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "true") boolean onlyVisible
    ) {
        List<SocialLinkResponse> links
                = socialLinkService.getUserSocialLinks(userId, onlyVisible);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("socialLinks", links);
        response.put("count", links.size());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> addSocialLink(
            @RequestParam Long userId,
            @Valid @RequestBody SocialLinkRequest request
    ) {
        try {
            SocialLinkResponse created
                    = socialLinkService.addSocialLink(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage())
            );
        }
    }

    @PutMapping("/{linkId}")
    public ResponseEntity<?> updateSocialLink(
            @RequestParam Long userId,
            @PathVariable Long linkId,
            @Valid @RequestBody SocialLinkRequest request
    ) {
        try {
            SocialLinkResponse updated
                    = socialLinkService.updateSocialLink(
                            userId,
                            linkId,
                            request
                    );

            return ResponseEntity.ok(updated);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage())
            );
        }
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<?> deleteSocialLink(
            @RequestParam Long userId,
            @PathVariable Long linkId
    ) {
        try {
            socialLinkService.deleteSocialLink(userId, linkId);

            return ResponseEntity.ok(
                    Map.of("message", "Social link deleted successfully")
            );

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage())
            );
        }
    }

    @PutMapping("/order")
    public ResponseEntity<?> updateDisplayOrder(
            @RequestParam Long userId,
            @RequestBody List<Long> linkIds
    ) {
        try {
            socialLinkService.updateDisplayOrder(userId, linkIds);

            return ResponseEntity.ok(
                    Map.of("message", "Display order updated successfully")
            );

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage())
            );
        }
    }

    @PutMapping("/batch")
    public ResponseEntity<?> updateAllSocialLinks(
            @RequestParam Long userId,
            @Valid @RequestBody List<@Valid SocialLinkRequest> requests
    ) {
        try {
            List<SocialLinkResponse> updated
                    = socialLinkService.updateAllSocialLinks(userId, requests);

            Map<String, Object> response = new HashMap<>();
            response.put("socialLinks", updated);
            response.put(
                    "message",
                    "All social links updated successfully"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", exception.getMessage())
            );
        }
    }

    @GetMapping("/platforms")
    public ResponseEntity<Map<String, Object>> getAvailablePlatforms() {

        List<Map<String, String>> platforms
                = java.util.Arrays.stream(SocialPlatform.values())
                        .map(platform -> {
                            Map<String, String> platformInfo
                                    = new HashMap<>();

                            platformInfo.put("name", platform.name());
                            platformInfo.put(
                                    "displayName",
                                    platform.getDisplayName()
                            );
                            platformInfo.put(
                                    "baseUrl",
                                    platform.getBaseUrl()
                            );

                            return platformInfo;
                        })
                        .toList();

        return ResponseEntity.ok(
                Map.of("platforms", platforms)
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateUrl(
            @RequestParam String url,
            @RequestParam SocialPlatform platform
    ) {
        boolean isValid
                = socialLinkService.validateUrl(url, platform);

        Map<String, Object> response = new HashMap<>();
        response.put("url", url);
        response.put("platform", platform);
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }
}
