package com.example.artship.entities.follow.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.example.artship.entities.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "follows")
@IdClass(Follow.FollowId.class)
public class Follow {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Follow() {
    }

    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Follow follow)) {
            return false;
        }

        Long followerId = follower != null ? follower.getId() : null;
        Long otherFollowerId = follow.follower != null
                ? follow.follower.getId() : null;
        Long followingId = following != null ? following.getId() : null;
        Long otherFollowingId = follow.following != null
                ? follow.following.getId() : null;

        return Objects.equals(followerId, otherFollowerId)
                && Objects.equals(followingId, otherFollowingId);
    }

    @Override
    public int hashCode() {
        Long followerId = follower != null ? follower.getId() : null;
        Long followingId = following != null ? following.getId() : null;
        return Objects.hash(followerId, followingId);
    }

    public static class FollowId implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long follower;
        private Long following;

        public FollowId() {
        }

        public FollowId(Long follower, Long following) {
            this.follower = follower;
            this.following = following;
        }

        public Long getFollower() {
            return follower;
        }

        public void setFollower(Long follower) {
            this.follower = follower;
        }

        public Long getFollowing() {
            return following;
        }

        public void setFollowing(Long following) {
            this.following = following;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FollowId followId)) {
                return false;
            }
            return Objects.equals(follower, followId.follower)
                    && Objects.equals(following, followId.following);
        }

        @Override
        public int hashCode() {
            return Objects.hash(follower, following);
        }
    }
}
