package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime publicationDate;
    
    private Boolean isPublished;
    private String authorId;
    private String authorName;
    
    // For Postman compatibility - author as string
    @JsonProperty("author")
    public String getAuthor() {
        return authorName != null ? authorName : (authorId != null ? authorId : "");
    }
    
    public void setAuthor(String author) {
        this.authorName = author;
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
