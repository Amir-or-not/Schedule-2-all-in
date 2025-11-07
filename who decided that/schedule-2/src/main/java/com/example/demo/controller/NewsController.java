package com.example.demo.controller;

import com.example.demo.dto.NewsDTO;
import com.example.demo.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Management", description = "APIs for managing news articles")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "Get all news")
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNewsList());
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published news")
    public ResponseEntity<Page<NewsDTO>> getPublishedNews(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(newsService.getPublishedNews(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get news by ID")
    public ResponseEntity<NewsDTO> getNewsById(
            @Parameter(description = "ID of the news article to retrieve")
            @PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new news article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NewsDTO> createNews(
            @Parameter(description = "News article details")
            @Valid @RequestBody NewsDTO newsDTO) {
        return new ResponseEntity<>(
                newsService.createNews(newsDTO),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing news article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NewsDTO> updateNews(
            @Parameter(description = "ID of the news article to update")
            @PathVariable Long id,
            @Parameter(description = "Updated news article details")
            @Valid @RequestBody NewsDTO newsDTO) {
        return ResponseEntity.ok(newsService.updateNews(id, newsDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a news article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNews(
            @Parameter(description = "ID of the news article to delete")
            @PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search news by title or content")
    public ResponseEntity<List<NewsDTO>> searchNews(
            @Parameter(description = "Search query")
            @RequestParam String query) {
        return ResponseEntity.ok(newsService.searchNewsList(query));
    }
}
