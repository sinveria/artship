package com.example.artship.features.collection.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.artship.entities.collection.model.CollectionArt;
import com.example.artship.features.collection.dto.CollectionArtDto;

@Component
public class CollectionArtMapper {

    public CollectionArtDto toDto(CollectionArt collectionArt) {
        if (collectionArt == null) {
            return null;
        }

        return new CollectionArtDto(collectionArt);
    }

    public List<CollectionArtDto> toDtos(
            List<CollectionArt> collectionArts
    ) {
        if (collectionArts == null || collectionArts.isEmpty()) {
            return Collections.emptyList();
        }

        return collectionArts.stream()
                .map(this::toDto)
                .toList();
    }
}