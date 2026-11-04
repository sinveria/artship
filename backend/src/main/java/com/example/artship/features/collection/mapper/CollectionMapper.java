package com.example.artship.features.collection.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.artship.entities.collection.model.Collection;
import com.example.artship.features.collection.dto.CollectionDto;

@Component
public class CollectionMapper {

    public CollectionDto toDto(Collection collection) {
        if (collection == null) {
            return null;
        }

        return new CollectionDto(collection);
    }

    public List<CollectionDto> toDtos(List<Collection> collections) {
        if (collections == null || collections.isEmpty()) {
            return Collections.emptyList();
        }

        return collections.stream()
                .map(this::toDto)
                .toList();
    }
}