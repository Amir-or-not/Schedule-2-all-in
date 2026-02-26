package com.example.demo.service;

import com.example.demo.dto.NewsDTO;
import com.example.demo.entity.News;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.NewsRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public Page<NewsDTO> getAllNews(Pageable pageable) {
        return newsRepository.findAllByOrderByPublicationDateDesc(pageable)
                .map(this::convertToDTO);
    }

    public List<NewsDTO> getAllNewsList() {
        return newsRepository.findAllByOrderByPublicationDateDesc(Pageable.unpaged())
                .map(this::convertToDTO)
                .stream()
                .collect(Collectors.toList());
    }

    public Page<NewsDTO> getPublishedNews(Pageable pageable) {
        return newsRepository.findByIsPublishedTrueOrderByPublicationDateDesc(pageable)
                .map(this::convertToDTO);
    }

    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        return convertToDTO(news);
    }

    @Transactional
    public NewsDTO createNews(NewsDTO newsDTO) {
        // Handle Postman format: author can be a string (author name)
        User author;
        if (newsDTO.getAuthor() != null && !newsDTO.getAuthor().isEmpty()) {
            // Try to find user by full name or use admin
            author = userRepository.findByFullNameContainingIgnoreCase(newsDTO.getAuthor())
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        // Use existing admin user from database
                        return userRepository.findByEmail("admin@example.com")
                                .orElseGet(() -> {
                                    // If admin doesn't exist, create one
                                    User adminUser = new User();
                                    adminUser.setUserId("admin");
                                    adminUser.setEmail("admin@example.com");
                                    adminUser.setFullName("Admin");
                                    adminUser.setRole("ROLE_ADMIN");
                                    adminUser.setPassword("$2a$10$XptfskLsT1SL/bOzZLikhOaQFiD6RJxFBX1pqpohsPYr6Q5LdUYxK");
                                    return userRepository.save(adminUser);
                                });
                    });
        } else {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            author = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        }
        
        News news = new News();
        news.setTitle(newsDTO.getTitle());
        news.setContent(newsDTO.getContent());
        news.setPublicationDate(newsDTO.getPublicationDate() != null ? 
                newsDTO.getPublicationDate() : LocalDateTime.now());
        news.setIsPublished(newsDTO.getIsPublished() != null ? newsDTO.getIsPublished() : false);
        news.setAuthor(author);
        
        News savedNews = newsRepository.save(news);
        log.info("[DATA] News created: id={}, title={}, authorId={}", savedNews.getId(), savedNews.getTitle(), savedNews.getAuthor().getUserId());
        return convertToDTO(savedNews);
    }

    @Transactional
    public NewsDTO updateNews(Long id, NewsDTO newsDTO) {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        
        // Check if the current user is the author or an admin
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!existingNews.getAuthor().getEmail().equals(currentEmail) && 
            !SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You are not authorized to update this news");
        }
        
        if (newsDTO.getTitle() != null) {
            existingNews.setTitle(newsDTO.getTitle());
        }
        if (newsDTO.getContent() != null) {
            existingNews.setContent(newsDTO.getContent());
        }
        if (newsDTO.getPublicationDate() != null) {
            existingNews.setPublicationDate(newsDTO.getPublicationDate());
        }
        if (newsDTO.getIsPublished() != null) {
            existingNews.setIsPublished(newsDTO.getIsPublished());
        }
        
        News updatedNews = newsRepository.save(existingNews);
        log.info("[DATA] News updated: id={}", id);
        return convertToDTO(updatedNews);
    }

    @Transactional
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News not found with id: " + id));
        
        // Check if the current user is the author or an admin
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!news.getAuthor().getEmail().equals(currentEmail) && 
            !SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You are not authorized to delete this news");
        }
        
        newsRepository.deleteById(id);
        log.info("[DATA] News deleted: id={}", id);
    }

    private NewsDTO convertToDTO(News news) {
        NewsDTO dto = modelMapper.map(news, NewsDTO.class);
        dto.setAuthorId(news.getAuthor().getUserId()); // User ID is a String in User entity
        dto.setAuthorName(news.getAuthor().getFullName());
        return dto;
    }

    public Page<NewsDTO> searchNews(String query, Pageable pageable) {
        return newsRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                query, pageable).map(this::convertToDTO);
    }

    public List<NewsDTO> searchNewsList(String query) {
        return newsRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                query, Pageable.unpaged())
                .map(this::convertToDTO)
                .stream()
                .collect(Collectors.toList());
    }
}
