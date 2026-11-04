package com.example.artship.entities.collection.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.example.artship.entities.art.model.Art;

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
@Table(name = "collection_arts")
@IdClass(CollectionArt.CollectionArtId.class)
public class CollectionArt {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_id", nullable = false)
    private Art art;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    public CollectionArt() {
    }

    public CollectionArt(Collection collection, Art art) {
        this.collection = collection;
        this.art = art;
        this.savedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CollectionArt that)) {
            return false;
        }

        return Objects.equals(
                collection != null ? collection.getId() : null,
                that.collection != null ? that.collection.getId() : null
        ) && Objects.equals(
                art != null ? art.getId() : null,
                that.art != null ? that.art.getId() : null
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                collection != null ? collection.getId() : null,
                art != null ? art.getId() : null
        );
    }

    @Override
    public String toString() {
        return "CollectionArt{" +
                "collectionId=" +
                (collection != null ? collection.getId() : null) +
                ", artId=" +
                (art != null ? art.getId() : null) +
                ", savedAt=" + savedAt +
                '}';
    }

    public static class CollectionArtId implements Serializable {

        private Long collection;
        private Long art;

        public CollectionArtId() {
        }

        public CollectionArtId(Long collection, Long art) {
            this.collection = collection;
            this.art = art;
        }

        public Long getCollection() {
            return collection;
        }

        public void setCollection(Long collection) {
            this.collection = collection;
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

            if (!(o instanceof CollectionArtId that)) {
                return false;
            }

            return Objects.equals(collection, that.collection)
                    && Objects.equals(art, that.art);
        }

        @Override
        public int hashCode() {
            return Objects.hash(collection, art);
        }
    }
}