package com.example.artship.features.sociallink.service;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.artship.entities.user.model.User;
import com.example.artship.features.sociallink.model.SocialLink;
import com.example.artship.features.sociallink.model.SocialPlatform;
import com.example.artship.features.sociallink.repository.SocialLinkRepository;
import com.example.artship.features.sociallink.request.SocialLinkRequest;
import com.example.artship.features.sociallink.response.SocialLinkResponse;

@ExtendWith(MockitoExtension.class)
class SocialLinkServiceTest {

    @Mock
    private SocialLinkRepository socialLinkRepository;

    @InjectMocks
    private SocialLinkService socialLinkService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L);
    }

    private SocialLinkRequest createRequest(
            SocialPlatform platform,
            String url
    ) {
        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform(platform);
        request.setUrl(url);
        request.setVisible(true);
        request.setDisplayOrder(0);
        return request;
    }

    private SocialLink createLink(
            Long linkId,
            User owner,
            SocialPlatform platform,
            String url
    ) {
        SocialLink link = new SocialLink(owner, platform, url);
        ReflectionTestUtils.setField(link, "id", linkId);
        link.setVisible(true);
        link.setDisplayOrder(0);
        return link;
    }

    @Test
    void getUserSocialLinksShouldReturnAllLinks() {
        SocialLink link = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository
                .findByUserOrderByDisplayOrderAsc(user))
                .thenReturn(List.of(link));

        List<SocialLinkResponse> result
                = socialLinkService.getUserSocialLinks(1L, false);

        assertEquals(1, result.size());
        assertEquals(
                SocialPlatform.TELEGRAM,
                result.get(0).getPlatform()
        );
        assertEquals(
                "https://t.me/testuser",
                result.get(0).getUrl()
        );

        verify(socialLinkRepository)
                .findByUserOrderByDisplayOrderAsc(user);
    }

    @Test
    void getUserSocialLinksShouldReturnOnlyVisibleLinks() {
        SocialLink link = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository
                .findByUserAndVisibleTrueOrderByDisplayOrderAsc(user))
                .thenReturn(List.of(link));

        List<SocialLinkResponse> result
                = socialLinkService.getUserSocialLinks(1L, true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isVisible());

        verify(socialLinkRepository)
                .findByUserAndVisibleTrueOrderByDisplayOrderAsc(user);
    }

    @Test
    void addSocialLinkShouldSaveAndReturnResponse() {
        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository.existsByUserAndPlatform(
                user,
                SocialPlatform.TELEGRAM
        )).thenReturn(false);

        when(socialLinkRepository.save(any(SocialLink.class)))
                .thenAnswer(invocation -> {
                    SocialLink saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 10L);
                    return saved;
                });

        SocialLinkResponse result
                = socialLinkService.addSocialLink(1L, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(
                SocialPlatform.TELEGRAM,
                result.getPlatform()
        );
        assertEquals(
                "https://t.me/testuser",
                result.getUrl()
        );

        verify(socialLinkRepository).save(any(SocialLink.class));
    }

    @Test
    void addSocialLinkShouldThrowWhenPlatformAlreadyExists() {
        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository.existsByUserAndPlatform(
                user,
                SocialPlatform.TELEGRAM
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.addSocialLink(1L, request)
        );

        verify(socialLinkRepository, never())
                .save(any(SocialLink.class));
    }

    @Test
    void addSocialLinkShouldThrowWhenUrlIsInvalid() {
        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "not-a-valid-url"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.addSocialLink(1L, request)
        );

        verifyNoInteractions(socialLinkRepository);
    }

    @Test
    void updateSocialLinkShouldUpdateExistingLink() {
        SocialLink link = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/olduser"
        );

        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/newuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(link));
        when(socialLinkRepository.save(link))
                .thenReturn(link);

        SocialLinkResponse result
                = socialLinkService.updateSocialLink(1L, 10L, request);

        assertEquals(
                "https://t.me/newuser",
                result.getUrl()
        );
        assertEquals(
                SocialPlatform.TELEGRAM,
                result.getPlatform()
        );

        verify(socialLinkRepository).save(link);
    }

    @Test
    void updateSocialLinkShouldThrowWhenLinkBelongsToAnotherUser() {
        SocialLink link = createLink(
                10L,
                new User(2L),
                SocialPlatform.TELEGRAM,
                "https://t.me/olduser"
        );

        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/newuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(link));

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateSocialLink(
                        1L,
                        10L,
                        request
                )
        );

        verify(socialLinkRepository, never())
                .save(any(SocialLink.class));
    }

    @Test
    void updateSocialLinkShouldThrowWhenLinkDoesNotExist() {
        SocialLinkRequest request = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/newuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateSocialLink(
                        1L,
                        10L,
                        request
                )
        );

        verify(socialLinkRepository, never())
                .save(any(SocialLink.class));
    }

    @Test
    void updateSocialLinkShouldThrowWhenNewPlatformAlreadyExists() {
        SocialLink link = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/olduser"
        );

        SocialLinkRequest request = createRequest(
                SocialPlatform.VKONTAKTE,
                "https://vk.com/testuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(link));
        when(socialLinkRepository.existsByUserAndPlatform(
                user,
                SocialPlatform.VKONTAKTE
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateSocialLink(
                        1L,
                        10L,
                        request
                )
        );

        verify(socialLinkRepository, never())
                .save(any(SocialLink.class));
    }

    @Test
    void deleteSocialLinkShouldDeleteOwnedLink() {
        SocialLink link = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(link));

        socialLinkService.deleteSocialLink(1L, 10L);

        verify(socialLinkRepository).delete(link);
    }

    @Test
    void deleteSocialLinkShouldThrowWhenLinkBelongsToAnotherUser() {
        SocialLink link = createLink(
                10L,
                new User(2L),
                SocialPlatform.TELEGRAM,
                "https://t.me/testuser"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(link));

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.deleteSocialLink(1L, 10L)
        );

        verify(socialLinkRepository, never())
                .delete(any(SocialLink.class));
    }

    @Test
    void updateDisplayOrderShouldUpdateAllLinks() {
        SocialLink first = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/first"
        );

        SocialLink second = createLink(
                11L,
                user,
                SocialPlatform.VKONTAKTE,
                "https://vk.com/second"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(first));
        when(socialLinkRepository.findById(11L))
                .thenReturn(Optional.of(second));

        socialLinkService.updateDisplayOrder(
                1L,
                List.of(10L, 11L)
        );

        assertEquals(0, first.getDisplayOrder());
        assertEquals(1, second.getDisplayOrder());

        verify(socialLinkRepository).saveAll(
                List.of(first, second)
        );
    }

    @Test
    void updateDisplayOrderShouldRejectDuplicateIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateDisplayOrder(
                        1L,
                        List.of(10L, 10L)
                )
        );

        verifyNoInteractions(socialLinkRepository);
    }

    @Test
    void updateDisplayOrderShouldNotChangeLinksWhenOwnershipFails() {
        SocialLink first = createLink(
                10L,
                user,
                SocialPlatform.TELEGRAM,
                "https://t.me/first"
        );

        SocialLink second = createLink(
                11L,
                new User(2L),
                SocialPlatform.VKONTAKTE,
                "https://vk.com/second"
        );

        when(socialLinkRepository.findById(10L))
                .thenReturn(Optional.of(first));
        when(socialLinkRepository.findById(11L))
                .thenReturn(Optional.of(second));

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateDisplayOrder(
                        1L,
                        List.of(10L, 11L)
                )
        );

        assertEquals(0, first.getDisplayOrder());
        assertEquals(0, second.getDisplayOrder());

        verify(socialLinkRepository, never())
                .saveAll(any());
    }

    @Test
    void updateAllSocialLinksShouldReplaceExistingLinks() {
        SocialLinkRequest first = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/first"
        );

        SocialLinkRequest second = createRequest(
                SocialPlatform.VKONTAKTE,
                "https://vk.com/second"
        );

        when(socialLinkRepository.saveAll(any(
                Iterable.class
        ))).thenAnswer(invocation -> {
            Iterable<SocialLink> links = invocation.getArgument(0);
            return links;
        });

        List<SocialLinkResponse> result
                = socialLinkService.updateAllSocialLinks(
                        1L,
                        List.of(first, second)
                );

        assertEquals(2, result.size());

        verify(socialLinkRepository).deleteAllByUser(user);
        verify(socialLinkRepository).saveAll(any(Iterable.class));
    }

    @Test
    void updateAllSocialLinksShouldValidateBeforeDeletingExistingLinks() {
        SocialLinkRequest valid = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/first"
        );

        SocialLinkRequest invalid = createRequest(
                SocialPlatform.VKONTAKTE,
                "invalid-url"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateAllSocialLinks(
                        1L,
                        List.of(valid, invalid)
                )
        );

        verify(socialLinkRepository, never())
                .deleteAllByUser(any(User.class));
        verify(socialLinkRepository, never())
                .saveAll(any(Iterable.class));
    }

    @Test
    void updateAllSocialLinksShouldRejectDuplicatePlatforms() {
        SocialLinkRequest first = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/first"
        );

        SocialLinkRequest second = createRequest(
                SocialPlatform.TELEGRAM,
                "https://t.me/second"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> socialLinkService.updateAllSocialLinks(
                        1L,
                        List.of(first, second)
                )
        );

        verify(socialLinkRepository, never())
                .deleteAllByUser(any(User.class));
    }

    @Test
    void validateUrlShouldReturnTrueForValidUrl() {
        assertTrue(
                socialLinkService.validateUrl(
                        "https://t.me/testuser",
                        SocialPlatform.TELEGRAM
                )
        );
    }

    @Test
    void validateUrlShouldReturnFalseForInvalidUrl() {
        assertFalse(
                socialLinkService.validateUrl(
                        "invalid-url",
                        SocialPlatform.TELEGRAM
                )
        );
    }

    @Test
    void validateUrlShouldReturnFalseWhenUrlIsNull() {
        assertFalse(
                socialLinkService.validateUrl(
                        null,
                        SocialPlatform.TELEGRAM
                )
        );
    }
}
