package com.aitor.blog.article.service;

import com.aitor.blog.article.dto.ArticleVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ArticleService {
    /**
     * 分页查询已发布文章。
     *
     * @param page         当前页码，从 1 开始
     * @param size         每页条数
     * @param categorySlug 分类英文标识，为空表示不筛选
     * @param keyword      标题/摘要关键字，为空表示不筛选
     * @return 文章卡片分页结果
     */
    Page<ArticleVO> listPublished(long page, long size, String categorySlug, String keyword);
}
