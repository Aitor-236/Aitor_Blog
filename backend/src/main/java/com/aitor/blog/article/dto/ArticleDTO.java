package com.aitor.blog.article.dto;

import lombok.Data;

@Data 
public class ArticleDTO {
    /** 文章ID，新增时为空，更新时必填 */
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String categoryName;
}
