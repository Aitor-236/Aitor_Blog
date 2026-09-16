package com.aitor.blog.article.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;

/**
 * 把文章实体的分页结果统一组装成 ArticleVO 分页结果：补上分类名称、
 * 只暴露前端需要的字段。公开列表和后台列表共用，保证两边返回结构一致。
 */
@Component
@RequiredArgsConstructor
public class ArticleVOAssembler {

    private final ArticleCategoryMapper articleCategoryMapper;

    Page<ArticleVO> toVoPage(Page<Article> articlePage) {
        Map<Long, String> categoryNames = loadCategoryNames(articlePage.getRecords());

        Page<ArticleVO> voPage = new Page<>(
                articlePage.getCurrent(),
                articlePage.getSize(),
                articlePage.getTotal());
        voPage.setRecords(articlePage.getRecords().stream()
                .map(article -> ArticleVO.from(article, categoryNames.get(article.getCategoryId())))
                .collect(Collectors.toList()));
        return voPage;
    }

    Page<ArticleVO> emptyPage(long page, long size) {
        Page<ArticleVO> empty = new Page<>(page, size);
        empty.setRecords(List.of());
        return empty;
    }

    private Map<Long, String> loadCategoryNames(List<Article> articles) {
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return articleCategoryMapper.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(ArticleCategory::getId, ArticleCategory::getName));
    }
}
