package com.example.artship.features.sociallink.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.user.model.User;
import com.example.artship.features.sociallink.model.SocialLink;
import com.example.artship.features.sociallink.model.SocialPlatform;
import com.example.artship.features.sociallink.repository.SocialLinkRepository;
import com.example.artship.features.sociallink.request.SocialLinkRequest;
import com.example.artship.features.sociallink.response.SocialLinkResponse;

@Service
@Transactional
public class SocialLinkService {

    private static final Logger logger
            = LoggerFactory.getLogger(SocialLinkService.class);

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$"
    );

    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkService(
            SocialLinkRepository socialLinkRepository
    ) {
        this.socialLinkRepository = socialLinkRepository;
    }

    @Transactional(readOnly = true)
    public List<SocialLinkResponse> getUserSocialLinks(
            Long userId,
            boolean onlyVisible
    ) {
        User user = new User(userId);

        List<SocialLink> links = onlyVisible
                ? socialLinkRepository
                        .findByUserAndVisibleTrueOrderByDisplayOrderAsc(user)
                : socialLinkRepository
                        .findByUserOrderByDisplayOrderAsc(user);

        return links.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public SocialLinkResponse addSocialLink(
            Long userId,
            SocialLinkRequest request
    ) {
        logger.info("Adding social link for user ID: {}", userId);

        validateRequest(request);

        User user = new User(userId);

        if (socialLinkRepository.existsByUserAndPlatform(
                user,
                request.getPlatform()
        )) {
            throw new IllegalArgumentException(
                    "Social link for platform "
                    + request.getPlatform().getDisplayName()
                    + " already exists"
            );
        }

        SocialLink socialLink = new SocialLink(
                user,
                request.getPlatform(),
                request.getUrl().trim()
        );

        socialLink.setVisible(request.isVisible());
        socialLink.setDisplayOrder(
                request.getDisplayOrder() == null
                ? 0
                : request.getDisplayOrder()
        );

        SocialLink saved = socialLinkRepository.save(socialLink);

        logger.info(
                "Social link added successfully with ID: {}",
                saved.getId()
        );

        return convertToResponse(saved);
    }

    public SocialLinkResponse updateSocialLink(
            Long userId,
            Long linkId,
            SocialLinkRequest request
    ) {
        logger.info(
                "Updating social link ID: {} for user ID: {}",
                linkId,
                userId
        );

        validateRequest(request);

        SocialLink socialLink = findSocialLink(linkId);
        checkOwnership(socialLink, userId);

        if (socialLink.getPlatform() != request.getPlatform()
                && socialLinkRepository.existsByUserAndPlatform(
                        socialLink.getUser(),
                        request.getPlatform()
                )) {
            throw new IllegalArgumentException(
                    "Social link for platform "
                    + request.getPlatform().getDisplayName()
                    + " already exists"
            );
        }

        socialLink.setPlatform(request.getPlatform());
        socialLink.setUrl(request.getUrl().trim());
        socialLink.setVisible(request.isVisible());
        socialLink.setDisplayOrder(
                request.getDisplayOrder() == null
                ? 0
                : request.getDisplayOrder()
        );

        SocialLink updated = socialLinkRepository.save(socialLink);

        logger.info("Social link updated successfully");

        return convertToResponse(updated);
    }

    public void deleteSocialLink(Long userId, Long linkId) {
        logger.info(
                "Deleting social link ID: {} for user ID: {}",
                linkId,
                userId
        );

        SocialLink socialLink = findSocialLink(linkId);
        checkOwnership(socialLink, userId);

        socialLinkRepository.delete(socialLink);

        logger.info("Social link deleted successfully");
    }

    public void updateDisplayOrder(
            Long userId,
            List<Long> linkIds
    ) {
        if (linkIds == null) {
            throw new IllegalArgumentException(
                    "Link IDs must not be null"
            );
        }

        if (new HashSet<>(linkIds).size() != linkIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate link IDs are not allowed"
            );
        }

        List<SocialLink> links = linkIds.stream()
                .map(this::findSocialLink)
                .toList();

        for (SocialLink link : links) {
            checkOwnership(link, userId);
        }

        for (int i = 0; i < links.size(); i++) {
            links.get(i).setDisplayOrder(i);
        }

        socialLinkRepository.saveAll(links);

        logger.info(
                "Display order updated successfully for user ID: {}",
                userId
        );
    }

    public List<SocialLinkResponse> updateAllSocialLinks(
            Long userId,
            List<SocialLinkRequest> requests
    ) {
        logger.info(
                "Updating all social links for user ID: {}",
                userId
        );

        if (requests == null) {
            throw new IllegalArgumentException(
                    "Social links list must not be null"
            );
        }

        Set<SocialPlatform> platforms = new HashSet<>();

        for (SocialLinkRequest request : requests) {
            validateRequest(request);

            if (!platforms.add(request.getPlatform())) {
                throw new IllegalArgumentException(
                        "Duplicate platform: "
                        + request.getPlatform().getDisplayName()
                );
            }
        }

        User user = new User(userId);

        List<SocialLink> newLinks = requests.stream()
                .map(request -> {
                    SocialLink link = new SocialLink(
                            user,
                            request.getPlatform(),
                            request.getUrl().trim()
                    );

                    link.setVisible(request.isVisible());
                    link.setDisplayOrder(
                            request.getDisplayOrder() == null
                            ? 0
                            : request.getDisplayOrder()
                    );

                    return link;
                })
                .toList();

        socialLinkRepository.deleteAllByUser(user);

        List<SocialLink> saved
                = socialLinkRepository.saveAll(newLinks);

        logger.info(
                "All social links updated successfully. Total: {}",
                saved.size()
        );

        return saved.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean validateUrl(
            String url,
            SocialPlatform platform
    ) {
        if (url == null
                || url.isBlank()
                || platform == null) {
            return false;
        }

        return URL_PATTERN.matcher(url.trim()).matches();
    }

    private void validateRequest(SocialLinkRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Social link request must not be null"
            );
        }

        if (request.getPlatform() == null) {
            throw new IllegalArgumentException(
                    "Platform is required"
            );
        }

        if (!validateUrl(request.getUrl(), request.getPlatform())) {
            throw new IllegalArgumentException(
                    "Invalid URL for platform "
                    + request.getPlatform().getDisplayName()
            );
        }

        if (request.getDisplayOrder() != null
                && request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException(
                    "Display order must not be negative"
            );
        }
    }

    private SocialLink findSocialLink(Long linkId) {
        return socialLinkRepository.findById(linkId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "Social link not found: " + linkId
                        )
                );
    }

    private void checkOwnership(
            SocialLink socialLink,
            Long userId
    ) {
        if (socialLink.getUser() == null
                || socialLink.getUser().getId() == null
                || !socialLink.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You don't have permission to manage this link"
            );
        }
    }

    private SocialLinkResponse convertToResponse(
            SocialLink socialLink
    ) {
        return new SocialLinkResponse(
                socialLink.getId(),
                socialLink.getPlatform(),
                socialLink.getUrl(),
                socialLink.isVisible(),
                socialLink.getDisplayOrder(),
                socialLink.getCreatedAt(),
                socialLink.getUpdatedAt()
        );
    }
}
