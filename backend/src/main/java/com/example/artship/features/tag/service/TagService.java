package com.example.artship.features.tag.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.repository.ArtRepository;
import com.example.artship.entities.tag.model.Tag;
import com.example.artship.entities.tag.repository.TagRepository;
import com.example.artship.features.tag.dto.TagDto;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final ArtRepository artRepository;

    public TagService(
            TagRepository tagRepository,
            ArtRepository artRepository
    ) {
        this.tagRepository = tagRepository;
        this.artRepository = artRepository;
    }

    public Tag createTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    @Transactional(readOnly = true)
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Tag::getName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Tag> searchTags(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByArtId(Long artId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Арт не найден: " + artId
                        )
                );

        return art.getTags();
    }

    @Transactional(readOnly = true)
    public List<Tag> getPopularTags(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return tagRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(
                        (Tag tag) -> tag.getArts() == null
                                ? 0
                                : tag.getArts().size()
                ).reversed())
                .limit(limit)
                .toList();
    }

    public Tag updateTag(Long id, String newName) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Тег не найден: " + id
                        )
                );

        if (!tag.getName().equals(newName)
                && tagRepository.existsByName(newName)) {
            throw new IllegalArgumentException(
                    "Тег с названием '" + newName + "' уже существует"
            );
        }

        tag.setName(newName);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Тег не найден: " + id
                        )
                );

        if (tag.getArts() != null) {
            for (Art art : tag.getArts()) {
                art.getTags().remove(tag);
            }

            artRepository.saveAll(tag.getArts());
        }

        tagRepository.delete(tag);
    }

    public Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    public List<Tag> createTags(List<String> tagNames) {
        return tagNames.stream()
                .map(this::findOrCreateTag)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean tagExists(String name) {
        return tagRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<TagDto> getTagDtoById(Long id) {
        return tagRepository.findById(id)
                .map(TagDto::new);
    }

    @Transactional(readOnly = true)
    public Optional<TagDto> getTagDtoByName(String name) {
        return tagRepository.findByName(name)
                .map(TagDto::new);
    }

    @Transactional(readOnly = true)
    public List<TagDto> getAllTagDtos() {
        return getAllTags()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDto> searchTagDtos(String name) {
        return searchTags(name)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTagDtosByArtId(Long artId) {
        return getTagsByArtId(artId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDto> getPopularTagDtos(int limit) {
        return getPopularTags(limit)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getArtCountByTagId(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Тег не найден: " + tagId
                        )
                );

        return tag.getArts() == null
                ? 0L
                : (long) tag.getArts().size();
    }

    private TagDto convertToDto(Tag tag) {
        TagDto dto = new TagDto(tag);

        dto.setArtCount(
                tag.getArts() == null
                        ? 0
                        : tag.getArts().size()
        );

        return dto;
    }
}