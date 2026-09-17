package com.aitor.blog.article.dto;

import java.time.LocalDateTime;

import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;

import lombok.Data;

/**
 * 公开文章详情，只暴露已发布文章用于前台展示的字段。
 * <p>
 * 与后台的 {@link ArticleDetailVO} 不同，这里不返回草稿状态等管理信息，
 * 正文以 Markdown 原文返回，由前端负责渲染。
 */
@Data
public class ArticlePublicDetailVO {

    private Long id;
    private String title;
    private String summary;

    /** Markdown 正文原文 */
    private String content;

    private String categoryName;
    private String categorySlug;

    /** 作者公开名称，目前取 sys_user.username */
    private String authorUsername;

    private Integer readingMinutes;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ArticlePublicDetailVO(Article article, ArticleCategory category, String authorUsername) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.content = article.getContentMarkdown();
        this.categoryName = category == null ? null : category.getName();
        this.categorySlug = category == null ? null : category.getSlug();
        this.authorUsername = authorUsername;
        this.readingMinutes = article.getReadingMinutes();
        this.publishedAt = article.getPublishedAt();
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
    }
}
