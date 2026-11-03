package com.example.artship.entities.art.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.artship.entities.art.model.Art;
import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.user.model.User;

@Repository
public interface ArtRepository extends JpaRepository<Art, Long> {

    Page<Art> findByAuthorAndIsPublicFlagTrue(User author, Pageable pageable);

    Page<Art> findByAuthor(User author, Pageable pageable);

    List<Art> findByAuthorId(Long authorId);

    void deleteByAuthorId(Long authorId);

    @Query("SELECT a FROM Art a WHERE a.isPublicFlag = true ORDER BY a.createdAt DESC")
    Page<Art> findByIsPublicFlagTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Art a WHERE a.isPublicFlag = true AND " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Art> findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(
            @Param("title") String title,
            Pageable pageable
    );

    long countByTitleContainingIgnoreCaseAndIsPublicFlagTrue(String title);

    Page<Art> findByTagsId(Long tagId, Pageable pageable);

    @Query("SELECT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) AND a.isPublicFlag = true " +
            "GROUP BY a.id HAVING COUNT(DISTINCT t.id) = :tagCount")
    Page<Art> findByTagNamesAndIsPublicFlagTrue(
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) AND a.isPublicFlag = true")
    Page<Art> findByAnyTagNamesAndIsPublicFlagTrue(
            @Param("tagNames") List<String> tagNames,
            Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT a) FROM Art a JOIN a.tags t " +
            "WHERE LOWER(t.name) = LOWER(:tagName) AND a.isPublicFlag = true")
    long countByTagName(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%')) " +
            "AND a.isPublicFlag = true")
    Page<Art> findByTagNameContainingIgnoreCaseAndIsPublicFlagTrue(
            @Param("tagName") String tagName,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE LOWER(t.name) = LOWER(:tagName) AND a.isPublicFlag = true")
    Page<Art> findByTagNameAndIsPublicFlagTrue(
            @Param("tagName") String tagName,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE a.status = :status " +
            "AND a.isPublicFlag = true ORDER BY a.createdAt DESC")
    Page<Art> findByStatusAndIsPublicFlagTrue(
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE a.status NOT IN :statuses " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByStatusNotIn(
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE a.author = :author " +
            "AND a.status NOT IN :statuses ORDER BY a.createdAt DESC")
    Page<Art> findByAuthorAndStatusNotIn(
            @Param("author") User author,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE a.author = :author " +
            "AND a.status = :status AND a.isPublicFlag = true " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByAuthorAndStatusAndIsPublicFlagTrue(
            @Param("author") User author,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE LOWER(a.title) " +
            "LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND a.status NOT IN :statuses ORDER BY a.createdAt DESC")
    Page<Art> findByTitleContainingIgnoreCaseAndStatusNotIn(
            @Param("title") String title,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a WHERE LOWER(a.title) " +
            "LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND a.status = :status AND a.isPublicFlag = true " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByTitleContainingIgnoreCaseAndStatusAndIsPublicFlagTrue(
            @Param("title") String title,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE LOWER(t.name) = LOWER(:tagName) " +
            "AND a.status NOT IN :statuses ORDER BY a.createdAt DESC")
    Page<Art> findByTagNameAndStatusNotIn(
            @Param("tagName") String tagName,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE LOWER(t.name) = LOWER(:tagName) " +
            "AND a.status = :status AND a.isPublicFlag = true " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByTagNameAndStatusAndIsPublicFlagTrue(
            @Param("tagName") String tagName,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) AND a.status NOT IN :statuses " +
            "GROUP BY a.id HAVING COUNT(DISTINCT t.id) = :tagCount " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByTagNamesAndStatusNotIn(
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) " +
            "AND a.status = :status AND a.isPublicFlag = true " +
            "GROUP BY a.id HAVING COUNT(DISTINCT t.id) = :tagCount " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByTagNamesAndStatusAndIsPublicFlagTrue(
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) AND a.status NOT IN :statuses " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByAnyTagNamesAndStatusNotIn(
            @Param("tagNames") List<String> tagNames,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a FROM Art a JOIN a.tags t " +
            "WHERE t.name IN (:tagNames) " +
            "AND a.status = :status AND a.isPublicFlag = true " +
            "ORDER BY a.createdAt DESC")
    Page<Art> findByAnyTagNamesAndStatusAndIsPublicFlagTrue(
            @Param("tagNames") List<String> tagNames,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    Page<Art> findByStatus(ArtStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Art a WHERE a.status = :status")
    long countByStatus(@Param("status") ArtStatus status);
}