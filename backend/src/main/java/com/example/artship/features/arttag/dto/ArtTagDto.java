package com.example.artship.features.arttag.dto;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.tag.model.Tag;

public class ArtTagDto {

    private Long artId;
    private String artTitle;

    private Long tagId;
    private String tagName;

    public ArtTagDto() {
    }

    public ArtTagDto(Art art, Tag tag) {
        if (art != null) {
            this.artId = art.getId();
            this.artTitle = art.getTitle();
        }

        if (tag != null) {
            this.tagId = tag.getId();
            this.tagName = tag.getName();
        }
    }

    public ArtTagDto(
            Long artId,
            String artTitle,
            Long tagId,
            String tagName
    ) {
        this.artId = artId;
        this.artTitle = artTitle;
        this.tagId = tagId;
        this.tagName = tagName;
    }

    public Long getArtId() {
        return artId;
    }

    public void setArtId(Long artId) {
        this.artId = artId;
    }

    public String getArtTitle() {
        return artTitle;
    }

    public void setArtTitle(String artTitle) {
        this.artTitle = artTitle;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return "ArtTagDto{"
                + "artId=" + artId
                + ", artTitle='" + artTitle + '\''
                + ", tagId=" + tagId
                + ", tagName='" + tagName + '\''
                + '}';
    }
}
