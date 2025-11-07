package com.example.demo.repository;

import com.example.demo.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    Page<News> findByIsPublishedTrueOrderByPublicationDateDesc(Pageable pageable);
    
    Page<News> findAllByOrderByPublicationDateDesc(Pageable pageable);
    
    // Using author.userId since the User entity uses userId as the ID field
    @Query("SELECT n FROM News n WHERE n.author.userId = :authorId ORDER BY n.publicationDate DESC")
    Page<News> findByAuthorIdOrderByPublicationDateDesc(@Param("authorId") String authorId, Pageable pageable);
    
    @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(concat('%', :query, '%'))")
    Page<News> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            @Param("query") String query, 
            Pageable pageable);
    
    @Query("SELECT n FROM News n WHERE n.author.userId = :authorId AND (LOWER(n.title) LIKE LOWER(concat('%', :query, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(concat('%', :query, '%'))) ORDER BY n.publicationDate DESC")
    Page<News> findByAuthorIdAndTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            @Param("authorId") String authorId,
            @Param("query") String query,
            Pageable pageable);
}
