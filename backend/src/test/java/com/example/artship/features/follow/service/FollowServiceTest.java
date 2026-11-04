package com.example.artship.features.follow.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.artship.entities.follow.model.Follow;
import com.example.artship.entities.follow.repository.FollowRepository;
import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.follow.dto.FollowDto;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    /**
     * Создание mock-пользователя без предварительных заглушек.
     * Нужные поля настраиваются непосредственно в тестах.
     */
    private User createUser() {
        return mock(User.class);
    }

    /**
     * Создание Follow для тестов, в которых DTO
     * должен получить информацию о пользователях.
     */
    private Follow createFollow(
            Long followerId,
            String followerUsername,
            Long followingId,
            String followingUsername
    ) {
        User follower = createUser();
        User following = createUser();

        when(follower.getId()).thenReturn(followerId);
        when(follower.getUsername()).thenReturn(followerUsername);
        when(follower.getDisplayName()).thenReturn(followerUsername);
        when(follower.getAvatarUrl()).thenReturn(null);

        when(following.getId()).thenReturn(followingId);
        when(following.getUsername()).thenReturn(followingUsername);
        when(following.getDisplayName()).thenReturn(followingUsername);
        when(following.getAvatarUrl()).thenReturn(null);

        return new Follow(follower, following);
    }

    @Test
    void followUserShouldCreateFollow() {
        User follower = createUser();
        User following = createUser();

        when(follower.getId()).thenReturn(1L);
        when(follower.getUsername()).thenReturn("user1");
        when(follower.getDisplayName()).thenReturn("user1");
        when(follower.getAvatarUrl()).thenReturn(null);

        when(following.getId()).thenReturn(2L);
        when(following.getUsername()).thenReturn("user2");
        when(following.getDisplayName()).thenReturn("user2");
        when(following.getAvatarUrl()).thenReturn(null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(following));

        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(false);

        when(followRepository.save(any(Follow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FollowDto result = followService.followUser(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getFollower().getId());
        assertEquals(2L, result.getFollowing().getId());

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void followUserShouldRejectNullIds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> followService.followUser(null, 2L)
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(followRepository);
    }

    @Test
    void followUserShouldRejectSelfFollow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> followService.followUser(1L, 1L)
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(followRepository);
    }

    @Test
    void followUserShouldRejectWhenFollowerDoesNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> followService.followUser(1L, 2L)
        );

        verify(userRepository).findById(1L);
        verify(userRepository, never()).findById(2L);
        verify(followRepository, never()).save(any());
    }

    @Test
    void followUserShouldRejectWhenFollowingUserDoesNotExist() {
        User follower = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> followService.followUser(1L, 2L)
        );

        verify(followRepository, never()).save(any());
    }

    @Test
    void followUserShouldRejectDuplicateFollow() {
        User follower = createUser();
        User following = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(following));

        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> followService.followUser(1L, 2L)
        );

        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollowUserShouldDeleteFollow() {
        Follow follow = new Follow(
                createUser(),
                createUser()
        );

        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.of(follow));

        followService.unfollowUser(1L, 2L);

        verify(followRepository).delete(follow);
    }

    @Test
    void unfollowUserShouldThrowWhenFollowDoesNotExist() {
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> followService.unfollowUser(1L, 2L)
        );

        verify(followRepository, never()).delete(any());
    }

    @Test
    void isFollowingShouldReturnTrue() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(true);

        assertTrue(followService.isFollowing(1L, 2L));
    }

    @Test
    void isFollowingShouldReturnFalse() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L))
                .thenReturn(false);

        assertFalse(followService.isFollowing(1L, 2L));
    }

    @Test
    void getFollowersShouldReturnPage() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.findByFollowingId(2L, pageable))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.getFollowers(2L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getFollower().getId());
    }

    @Test
    void getFollowingShouldReturnPage() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.findByFollowerId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.getFollowing(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(2L, result.getContent().get(0).getFollowing().getId());
    }

    @Test
    void getFollowersWithoutPaginationShouldReturnList() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");

        when(followRepository.findByFollowingId(2L))
                .thenReturn(List.of(follow));

        List<FollowDto> result = followService.getFollowers(2L);

        assertEquals(1, result.size());
    }

    @Test
    void getFollowingWithoutPaginationShouldReturnList() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");

        when(followRepository.findByFollowerId(1L))
                .thenReturn(List.of(follow));

        List<FollowDto> result = followService.getFollowing(1L);

        assertEquals(1, result.size());
    }

    @Test
    void searchFollowersShouldReturnMatchingResults() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.searchFollowersByUsername(
                2L,
                "user",
                pageable
        )).thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.searchFollowersByUsername(
                        2L,
                        "user",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchFollowersShouldReturnAllWhenQueryIsBlank() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.findByFollowingId(2L, pageable))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.searchFollowersByUsername(
                        2L,
                        " ",
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(followRepository, never())
                .searchFollowersByUsername(
                        anyLong(),
                        anyString(),
                        any()
                );
    }

    @Test
    void searchFollowingShouldReturnMatchingResults() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.searchFollowingByUsername(
                1L,
                "user",
                pageable
        )).thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.searchFollowingByUsername(
                        1L,
                        "user",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchFollowingShouldReturnAllWhenQueryIsBlank() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.findByFollowerId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.searchFollowingByUsername(
                        1L,
                        "",
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        verify(followRepository, never())
                .searchFollowingByUsername(
                        anyLong(),
                        anyString(),
                        any()
                );
    }

    @Test
    void searchFollowersShouldNotExcludeOtherUsers() {
        Follow follow = createFollow(1L, "user1", 2L, "user2");
        PageRequest pageable = PageRequest.of(0, 10);

        when(followRepository.searchFollowersByUsername(
                2L,
                "user",
                pageable
        )).thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowDto> result =
                followService.searchFollowersByUsername(
                        2L,
                        "user",
                        pageable,
                        true
                );

        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getFollower().getId());
    }

    @Test
    void getFollowerCountShouldReturnCount() {
        when(followRepository.countByFollowingId(2L))
                .thenReturn(5L);

        assertEquals(5L, followService.getFollowerCount(2L));
    }

    @Test
    void getFollowingCountShouldReturnCount() {
        when(followRepository.countByFollowerId(1L))
                .thenReturn(3L);

        assertEquals(3L, followService.getFollowingCount(1L));
    }

    @Test
    void countFollowersBySearchShouldReturnCount() {
        when(followRepository.countFollowersByUsername(2L, "user"))
                .thenReturn(4L);

        assertEquals(
                4L,
                followService.countFollowersBySearch(2L, "user")
        );
    }

    @Test
    void countFollowersBySearchShouldReturnTotalWhenQueryIsBlank() {
        when(followRepository.countByFollowingId(2L))
                .thenReturn(7L);

        assertEquals(
                7L,
                followService.countFollowersBySearch(2L, " ")
        );

        verify(followRepository, never())
                .countFollowersByUsername(anyLong(), anyString());
    }

    @Test
    void countFollowingBySearchShouldReturnCount() {
        when(followRepository.countFollowingByUsername(1L, "user"))
                .thenReturn(2L);

        assertEquals(
                2L,
                followService.countFollowingBySearch(1L, "user")
        );
    }

    @Test
    void countFollowingBySearchShouldReturnTotalWhenQueryIsBlank() {
        when(followRepository.countByFollowerId(1L))
                .thenReturn(6L);

        assertEquals(
                6L,
                followService.countFollowingBySearch(1L, "")
        );

        verify(followRepository, never())
                .countFollowingByUsername(anyLong(), anyString());
    }

    @Test
    void deleteAllUserFollowsShouldDeleteBothDirections() {
        followService.deleteAllUserFollows(1L);

        verify(followRepository).deleteAllByFollowerId(1L);
        verify(followRepository).deleteAllByFollowingId(1L);
    }
}