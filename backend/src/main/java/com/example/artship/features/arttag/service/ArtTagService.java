package com.example.artship.features.arttag.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.tag.model.Tag;
import com.example.artship.entities.tag.repository.TagRepository;
import com.example.artship.features.arttag.dto.ArtTagDto;
import com.example.artship.features.tag.service.TagService;

@Service
@Transactional
public class ArtTagService {

    private final ArtRepository artRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

    public ArtTagService(
            ArtRepository artRepository,
            TagRepository tagRepository,
            TagService tagService
    ) {
        this.artRepository = artRepository;
        this.tagRepository = tagRepository;
        this.tagService = tagService;
    }

    public ArtTagDto createArtTag(Long artId, Long tagId) {
        Art art = getArtById(artId);
        Tag tag = getTagById(tagId);

        if (art.getTags().contains(tag)) {
            throw new IllegalArgumentException(
                    "Этот тег уже добавлен к работе"
            );
        }

        art.getTags().add(tag);
        artRepository.save(art);

        if (tag.getArts() != null && !tag.getArts().contains(art)) {
            tag.getArts().add(art);
        }

        return new ArtTagDto(art, tag);
    }

    @Transactional(readOnly = true)
    public Optional<ArtTagDto> getArtTagDtoByArtIdAndTagId(
            Long artId,
            Long tagId
    ) {
        Art art = getArtById(artId);

        return art.getTags()
                .stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst()
                .map(tag -> new ArtTagDto(art, tag));
    }

    public void removeTagFromArt(Long artId, Long tagId) {
        Art art = getArtById(artId);
        Tag tag = getTagById(tagId);

        boolean removed = art.getTags().removeIf(
                existingTag -> existingTag.getId().equals(tagId)
        );

        if (!removed) {
            throw new IllegalArgumentException(
                    "У этой работы нет указанного тега"
            );
        }

        artRepository.save(art);

        if (tag.getArts() != null) {
            tag.getArts().removeIf(
                    existingArt -> existingArt.getId().equals(artId)
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByArtIdAndTagId(Long artId, Long tagId) {
        return artRepository.findById(artId)
                .map(art -> art.getTags()
                        .stream()
                        .anyMatch(tag -> tag.getId().equals(tagId)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<ArtTagDto> getArtTagDtosByArtId(
            Long artId,
            Pageable pageable
    ) {
        Art art = getArtById(artId);

        List<ArtTagDto> relations = art.getTags()
                .stream()
                .sorted(Comparator.comparing(Tag::getId))
                .map(tag -> new ArtTagDto(art, tag))
                .toList();

        return createPage(relations, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtTagDto> getArtTagDtosByTagId(
            Long tagId,
            Pageable pageable
    ) {
        Tag tag = getTagById(tagId);

        List<ArtTagDto> relations = tag.getArts() == null
                ? List.of()
                : tag.getArts()
                        .stream()
                        .sorted(Comparator.comparing(Art::getId))
                        .map(art -> new ArtTagDto(art, tag))
                        .toList();

        return createPage(relations, pageable);
    }

    @Transactional(readOnly = true)
    public Long getArtCountByTagId(Long tagId) {
        getTagById(tagId);
        return tagRepository.countByArtsId(tagId);
    }

    @Transactional(readOnly = true)
    public Long getTagCountByArtId(Long artId) {
        Art art = getArtById(artId);
        return (long) art.getTags().size();
    }

    public void removeAllTagsFromArt(Long artId) {
        Art art = getArtById(artId);

        List<Tag> tags = new ArrayList<>(art.getTags());

        art.getTags().clear();
        artRepository.save(art);

        for (Tag tag : tags) {
            if (tag.getArts() != null) {
                tag.getArts().removeIf(
                        existingArt -> existingArt.getId().equals(artId)
                );
            }
        }
    }

    public void addTagsToArt(Long artId, List<String> tagNames) {
        Art art = getArtById(artId);

        if (tagNames == null || tagNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Необходимо передать хотя бы один тег"
            );
        }

        for (String tagName : tagNames) {
            if (tagName == null || tagName.isBlank()) {
                continue;
            }

            Tag tag = tagService.findOrCreateTag(tagName.trim());

            boolean alreadyAdded = art.getTags()
                    .stream()
                    .anyMatch(existingTag
                            -> existingTag.getId().equals(tag.getId())
                    );

            if (!alreadyAdded) {
                art.getTags().add(tag);

                if (tag.getArts() != null
                        && !tag.getArts().contains(art)) {
                    tag.getArts().add(art);
                }
            }
        }

        artRepository.save(art);
    }

    private Art getArtById(Long artId) {
        return artRepository.findById(artId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "Работа не найдена: " + artId
                        )
                );
    }

    private Tag getTagById(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                                "Тег не найден: " + tagId
                        )
                );
    }

    private <T> Page<T> createPage(
            List<T> content,
            Pageable pageable
    ) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(content);
        }

        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();

        if (offset >= content.size()) {
            return new PageImpl<>(List.of(), pageable, content.size());
        }

        int start = (int) offset;
        int end = Math.min(start + pageSize, content.size());

        return new PageImpl<>(
                content.subList(start, end),
                pageable,
                content.size()
        );
    }
}
