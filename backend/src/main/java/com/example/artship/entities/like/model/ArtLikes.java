package com.example.artship.entities.like.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.example.artship.entities.art.model.Art;
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
@Table(name = "art_likes")
@IdClass(ArtLikes.LikeId.class)
public class ArtLikes {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_id", nullable = false)
    private Art art;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ArtLikes() {
    }

    public ArtLikes(User user, Art art) {
        this.user = user;
        this.art = art;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
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

        if (!(o instanceof ArtLikes that)) {
            return false;
        }

        return Objects.equals(user, that.user)
                && Objects.equals(art, that.art);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, art);
    }

    @Override
    public String toString() {
        return "ArtLikes{"
                + "userId=" + (user != null ? user.getId() : null)
                + ", artId=" + (art != null ? art.getId() : null)
                + ", createdAt=" + createdAt
                + '}';
    }

    public static class LikeId implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long user;
        private Long art;

        public LikeId() {
        }

        public LikeId(Long user, Long art) {
            this.user = user;
            this.art = art;
        }

        public Long getUser() {
            return user;
        }

        public void setUser(Long user) {
            this.user = user;
        }

        public Long getArt() {
            return art;
        }

        public void setArt(Long art) {
            this.art = art;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof LikeId likeId)) {
                return false;
            }

            return Objects.equals(user, likeId.user)
                    && Objects.equals(art, likeId.art);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, art);
        }
    }
}
