package com.example.demo.controller;

import com.example.demo.dto.NewsDTO;
import com.example.demo.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News Management", description = "APIs for managing news articles")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    @Operation(summary = "Get all news")
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        log.debug("GET / - getAllNews()");
        List<NewsDTO> list = newsService.getAllNewsList();
        log.info("GET / - getAllNews() returned {} items", list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published news")
    public ResponseEntity<Page<NewsDTO>> getPublishedNews(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(sort = "publicationDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("GET /published - getPublishedNews(page={}, size={})", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(newsService.getPublishedNews(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get news by ID")
    public ResponseEntity<NewsDTO> getNewsById(
            @Parameter(description = "ID of the news article to retrieve")
            @PathVariable Long id) {
        log.debug("GET /{} - getNewsById(id={})", id, id);
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new news article")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsDTO> createNews(
            @Parameter(description = "News article details")
            @Valid @RequestBody NewsDTO newsDTO) {
        log.debug("POST / - createNews(title={})", newsDTO.getTitle());
        NewsDTO created = newsService.createNews(newsDTO);
        log.info("POST / - news created: id={}, title={}", created.getId(), created.getTitle());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing news article")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsDTO> updateNews(
            @Parameter(description = "ID of the news article to update")
            @PathVariable Long id,
            @Parameter(description = "Updated news article details")
            @Valid @RequestBody NewsDTO newsDTO) {
        log.debug("PUT /{} - updateNews(id={})", id, id);
        NewsDTO updated = newsService.updateNews(id, newsDTO);
        log.info("PUT /{} - news updated", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a news article")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNews(
            @Parameter(description = "ID of the news article to delete")
            @PathVariable Long id) {
        log.debug("DELETE /{} - deleteNews(id={})", id, id);
        newsService.deleteNews(id);
        log.info("DELETE /{} - news deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search news by title or content")
    public ResponseEntity<List<NewsDTO>> searchNews(
            @Parameter(description = "Search query")
            @RequestParam String query) {
        log.debug("GET /search?query={} - searchNews()", query);
        List<NewsDTO> list = newsService.searchNewsList(query);
        log.info("GET /search - query='{}' returned {} items", query, list.size());
        return ResponseEntity.ok(list);
    }
}
