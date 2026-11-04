package com.example.artship.features.collection.dto;

public class MoveArtRequest {

    private Long fromCollectionId;
    private Long toCollectionId;

    public MoveArtRequest() {
    }

    public Long getFromCollectionId() {
        return fromCollectionId;
    }

    public void setFromCollectionId(Long fromCollectionId) {
        this.fromCollectionId = fromCollectionId;
    }

    public Long getToCollectionId() {
        return toCollectionId;
    }

    public void setToCollectionId(Long toCollectionId) {
        this.toCollectionId = toCollectionId;
    }
}