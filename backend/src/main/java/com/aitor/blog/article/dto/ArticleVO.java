package com.aitor.blog.article.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;
    private String categoryName;
    private LocalDateTime publishedAt;
    private Integer readingMinutes;
}
