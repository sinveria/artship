package com.example.artship.entities.collection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.entities.art.model.ArtStatus;
import com.example.artship.entities.collection.model.CollectionArt;

@Repository
public interface CollectionArtRepository extends
        JpaRepository<CollectionArt, CollectionArt.CollectionArtId> {

    Page<CollectionArt> findByCollectionId(
            Long collectionId,
            Pageable pageable
    );

    Page<CollectionArt> findByArtId(
            Long artId,
            Pageable pageable
    );

    List<CollectionArt> findByCollectionId(Long collectionId);

    List<CollectionArt> findByArtId(Long artId);

    Optional<CollectionArt> findByCollectionIdAndArtId(
            Long collectionId,
            Long artId
    );

    boolean existsByCollectionIdAndArtId(
            Long collectionId,
            Long artId
    );

    @Modifying
    @Transactional
    void deleteByCollectionIdAndArtId(Long collectionId, Long artId);

    @Modifying
    @Transactional
    void deleteByCollectionId(Long collectionId);

    Long countByCollectionId(Long collectionId);

    @Query("""
            SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END
            FROM CollectionArt ca
            WHERE ca.art.id = :artId
            AND ca.collection.user.id != :userId
            """)
    boolean existsByArtIdAndUserIdNot(
            @Param("artId") Long artId,
            @Param("userId") Long userId
    );

    @Modifying
    @Transactional
    void deleteByArtId(Long artId);

    @Query("""
            SELECT ca FROM CollectionArt ca
            WHERE ca.collection.id = :collectionId
            AND ca.art.status = :status
            """)
    Page<CollectionArt> findByCollectionIdAndArtStatus(
            @Param("collectionId") Long collectionId,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT ca FROM CollectionArt ca
            WHERE ca.collection.id = :collectionId
            AND ca.art.status IN :statuses
            """)
    Page<CollectionArt> findByCollectionIdAndArtStatusIn(
            @Param("collectionId") Long collectionId,
            @Param("statuses") List<ArtStatus> statuses,
            Pageable pageable
    );

    @Query("""
            SELECT ca FROM CollectionArt ca
            WHERE ca.collection.id = :collectionId
            AND ca.art.status = :status
            """)
    List<CollectionArt> findByCollectionIdAndArtStatus(
            @Param("collectionId") Long collectionId,
            @Param("status") ArtStatus status
    );

    @Query("""
            SELECT COUNT(ca) FROM CollectionArt ca
            WHERE ca.collection.id = :collectionId
            AND ca.art.status = :status
            """)
    Long countByCollectionIdAndArtStatus(
            @Param("collectionId") Long collectionId,
            @Param("status") ArtStatus status
    );

    @Query("""
            SELECT COUNT(ca) FROM CollectionArt ca
            WHERE ca.collection.id = :collectionId
            AND ca.art.status IN :statuses
            """)
    Long countByCollectionIdAndArtStatusIn(
            @Param("collectionId") Long collectionId,
            @Param("statuses") List<ArtStatus> statuses
    );

    @Query("""
            SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END
            FROM CollectionArt ca
            WHERE ca.art.id = :artId
            AND ca.collection.user.id != :userId
            AND ca.art.status = :status
            """)
    boolean existsActiveByArtIdAndUserIdNot(
            @Param("artId") Long artId,
            @Param("userId") Long userId,
            @Param("status") ArtStatus status
    );

    @Query("""
            SELECT ca FROM CollectionArt ca
            WHERE ca.art.id = :artId
            AND ca.art.status = :status
            """)
    List<CollectionArt> findByArtIdAndArtStatus(
            @Param("artId") Long artId,
            @Param("status") ArtStatus status
    );

    @Query("""
            SELECT ca FROM CollectionArt ca
            WHERE ca.art.id = :artId
            AND ca.art.status = :status
            """)
    Page<CollectionArt> findByArtIdAndArtStatus(
            @Param("artId") Long artId,
            @Param("status") ArtStatus status,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CollectionArt ca
            WHERE ca.art.status = :status
            """)
    void deleteByArtStatus(@Param("status") ArtStatus status);

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CollectionArt ca
            WHERE ca.art.author.id = :userId
            """)
    void deleteByAuthorId(@Param("userId") Long userId);
}