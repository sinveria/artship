package com.example.artship.features.art.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.artship.entities.art.model.Art;
import com.example.artship.features.art.dto.ArtDto;
import com.example.artship.features.tag.dto.TagDto;

@Component
public class ArtMapper {

    public ArtDto toDto(Art art) {
        if (art == null) {
            return null;
        }

        return new ArtDto(art);
    }

    public ArtDto toDto(Art art, List<TagDto> tags) {
        if (art == null) {
            return null;
        }

        return new ArtDto(art, tags);
    }

    public List<ArtDto> toDtoList(List<Art> arts) {
        if (arts == null) {
            return List.of();
        }

        return arts.stream()
                .map(this::toDto)
                .toList();
    }
}