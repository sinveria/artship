package com.example.artship.entities.tag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.artship.entities.tag.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String name);

    Page<Tag> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    List<Tag> findByNameIn(List<String> names);

    long countByArtsId(Long artId);
}