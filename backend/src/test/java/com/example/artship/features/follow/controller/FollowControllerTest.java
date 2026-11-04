package com.example.artship.features.follow.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.follow.dto.FollowDto;
import com.example.artship.features.follow.service.FollowService;

@ExtendWith(MockitoExtension.class)
class FollowControllerTest {

    @Mock
    private FollowService followService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowController followController;

    private final Principal principal = () -> "testuser";

    /**
     * Настройка авторизованного пользователя.
     * Вызывается только в тестах, которым нужен текущий пользователь.
     */
    private void mockAuthenticatedUser() {
        User currentUser = mock(User.class);

        when(currentUser.getId()).thenReturn(1L);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(currentUser));
    }

    @Test
    void followUserShouldReturnOk() {
        mockAuthenticatedUser();

        FollowDto followDto = mock(FollowDto.class);

        when(followService.followUser(1L, 2L))
                .thenReturn(followDto);

        ResponseEntity<FollowDto> response =
                followController.followUser(principal, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(followDto, response.getBody());

        verify(followService).followUser(1L, 2L);
    }

    @Test
    void followUserShouldRejectUnauthenticatedUser() {
        assertThrows(
                ResponseStatusException.class,
                () -> followController.followUser(null, 2L)
        );

        verifyNoInteractions(followService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void followUserShouldRejectWhenAuthenticatedUserNotFound() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> followController.followUser(principal, 2L)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(followService);
    }

    @Test
    void unfollowUserShouldReturnOk() {
        mockAuthenticatedUser();

        ResponseEntity<Map<String, String>> response =
                followController.unfollowUser(principal, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Unfollowed successfully",
                response.getBody().get("message")
        );

        verify(followService).unfollowUser(1L, 2L);
    }

    @Test
    void isFollowingShouldReturnTrue() {
        mockAuthenticatedUser();

        when(followService.isFollowing(1L, 2L))
                .thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response =
                followController.isFollowing(principal, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("isFollowing"));
    }

    @Test
    void isFollowingShouldReturnFalse() {
        mockAuthenticatedUser();

        when(followService.isFollowing(1L, 2L))
                .thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response =
                followController.isFollowing(principal, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody().get("isFollowing"));
    }

    @Test
    void searchFollowersShouldReturnResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.searchFollowersByUsername(
                2L,
                "user",
                pageable
        )).thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.searchFollowers(
                        2L,
                        "user",
                        pageable
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void searchFollowingShouldReturnResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.searchFollowingByUsername(
                1L,
                "user",
                pageable
        )).thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.searchFollowing(
                        1L,
                        "user",
                        pageable
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void searchMyFollowersShouldReturnResults() {
        mockAuthenticatedUser();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.searchFollowersByUsername(
                1L,
                "user",
                pageable
        )).thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.searchMyFollowers(
                        principal,
                        "user",
                        pageable
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void searchMyFollowingShouldReturnResults() {
        mockAuthenticatedUser();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.searchFollowingByUsername(
                1L,
                "user",
                pageable
        )).thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.searchMyFollowing(
                        principal,
                        "user",
                        pageable
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getFollowersShouldReturnResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.getFollowers(2L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.getFollowers(2L, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getFollowingShouldReturnResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.getFollowing(2L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.getFollowing(2L, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getMyFollowersShouldReturnResults() {
        mockAuthenticatedUser();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.getFollowers(1L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.getMyFollowers(principal, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getMyFollowingShouldReturnResults() {
        mockAuthenticatedUser();

        PageRequest pageable = PageRequest.of(0, 20);
        Page<FollowDto> page = new PageImpl<>(List.of(
                mock(FollowDto.class)
        ));

        when(followService.getFollowing(1L, pageable))
                .thenReturn(page);

        ResponseEntity<Page<FollowDto>> response =
                followController.getMyFollowing(principal, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getFollowCountsShouldReturnBothCounts() {
        when(followService.getFollowerCount(2L))
                .thenReturn(10L);

        when(followService.getFollowingCount(2L))
                .thenReturn(5L);

        ResponseEntity<Map<String, Long>> response =
                followController.getFollowCounts(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().get("followers"));
        assertEquals(5L, response.getBody().get("following"));
    }

    @Test
    void searchMyFollowersShouldRejectUnauthenticatedUser() {
        PageRequest pageable = PageRequest.of(0, 20);

        assertThrows(
                ResponseStatusException.class,
                () -> followController.searchMyFollowers(
                        null,
                        "user",
                        pageable
                )
        );

        verifyNoInteractions(followService);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getMyFollowingShouldRejectUnauthenticatedUser() {
        PageRequest pageable = PageRequest.of(0, 20);

        assertThrows(
                ResponseStatusException.class,
                () -> followController.getMyFollowing(null, pageable)
        );

        verifyNoInteractions(followService);
        verifyNoInteractions(userRepository);
    }
}