package com.example.artship.features.follow.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.follow.dto.FollowDto;
import com.example.artship.features.follow.service.FollowService;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    public FollowController(
            FollowService followService,
            UserRepository userRepository
    ) {
        this.followService = followService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );

        return user.getId();
    }

    @PostMapping("/{followingId}")
    public ResponseEntity<FollowDto> followUser(
            Principal principal,
            @PathVariable Long followingId
    ) {

        Long currentUserId = getCurrentUserId(principal);

        FollowDto follow = followService.followUser(
                currentUserId,
                followingId
        );

        return ResponseEntity.ok(follow);
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            Principal principal,
            @PathVariable Long followingId
    ) {

        Long currentUserId = getCurrentUserId(principal);

        followService.unfollowUser(
                currentUserId,
                followingId
        );

        return ResponseEntity.ok(
                Map.of("message", "Unfollowed successfully")
        );
    }

    @GetMapping("/check/{followingId}")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            Principal principal,
            @PathVariable Long followingId
    ) {

        Long currentUserId = getCurrentUserId(principal);

        boolean following = followService.isFollowing(
                currentUserId,
                followingId
        );

        return ResponseEntity.ok(
                Map.of("isFollowing", following)
        );
    }

    @GetMapping("/followers/{userId}/search")
    public ResponseEntity<Page<FollowDto>> searchFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<FollowDto> followers =
                followService.searchFollowersByUsername(
                        userId,
                        username,
                        pageable
                );

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}/search")
    public ResponseEntity<Page<FollowDto>> searchFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<FollowDto> following =
                followService.searchFollowingByUsername(
                        userId,
                        username,
                        pageable
                );

        return ResponseEntity.ok(following);
    }

    @GetMapping("/me/followers/search")
    public ResponseEntity<Page<FollowDto>> searchMyFollowers(
            Principal principal,
            @RequestParam(required = false) String username,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Long currentUserId = getCurrentUserId(principal);

        Page<FollowDto> followers =
                followService.searchFollowersByUsername(
                        currentUserId,
                        username,
                        pageable
                );

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/me/following/search")
    public ResponseEntity<Page<FollowDto>> searchMyFollowing(
            Principal principal,
            @RequestParam(required = false) String username,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Long currentUserId = getCurrentUserId(principal);

        Page<FollowDto> following =
                followService.searchFollowingByUsername(
                        currentUserId,
                        username,
                        pageable
                );

        return ResponseEntity.ok(following);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<Page<FollowDto>> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<FollowDto> followers =
                followService.getFollowers(userId, pageable);

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<Page<FollowDto>> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<FollowDto> following =
                followService.getFollowing(userId, pageable);

        return ResponseEntity.ok(following);
    }

    @GetMapping("/me/followers")
    public ResponseEntity<Page<FollowDto>> getMyFollowers(
            Principal principal,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Long currentUserId = getCurrentUserId(principal);

        Page<FollowDto> followers =
                followService.getFollowers(currentUserId, pageable);

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/me/following")
    public ResponseEntity<Page<FollowDto>> getMyFollowing(
            Principal principal,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Long currentUserId = getCurrentUserId(principal);

        Page<FollowDto> following =
                followService.getFollowing(currentUserId, pageable);

        return ResponseEntity.ok(following);
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Long>> getFollowCounts(
            @PathVariable Long userId
    ) {

        Map<String, Long> counts = Map.of(
                "followers", followService.getFollowerCount(userId),
                "following", followService.getFollowingCount(userId)
        );

        return ResponseEntity.ok(counts);
    }
}