package com.aitor.blog.article.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitor.blog.article.dto.ArticleVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.aitor.blog.article.mapper.ArticleMapper;
import com.aitor.blog.article.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String STATUS_PUBLISHED = "published";

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper articleCategoryMapper;

    @Override
    public Page<ArticleVO> listPublished(long page, long size, String categorySlug, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, STATUS_PUBLISHED)
                .orderByDesc(Article::getPublishedAt);

        if (StringUtils.hasText(categorySlug)) {
            ArticleCategory category = articleCategoryMapper.selectOne(
                    new LambdaQueryWrapper<ArticleCategory>()
                            .eq(ArticleCategory::getSlug, categorySlug));
            if (category == null) {
                return emptyPage(page, size);
            }
            wrapper.eq(Article::getCategoryId, category.getId());
        }

        if (StringUtils.hasText(keyword)) {
            String likeKeyword = keyword.trim();
            wrapper.and(w -> w.like(Article::getTitle, likeKeyword)
                    .or()
                    .like(Article::getSummary, likeKeyword));
        }

        Page<Article> articlePage = articleMapper.selectPage(new Page<>(page, size), wrapper);

        Map<Long, String> categoryNames = loadCategoryNames(articlePage.getRecords());

        Page<ArticleVO> voPage = new Page<>(
                articlePage.getCurrent(),
                articlePage.getSize(),
                articlePage.getTotal());
        voPage.setRecords(articlePage.getRecords().stream()
                .map(toArticleVO(categoryNames))
                .collect(Collectors.toList()));
        return voPage;
    }

    private Map<Long, String> loadCategoryNames(List<Article> articles) {
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return articleCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(ArticleCategory::getId, ArticleCategory::getName));
    }

    private Function<Article, ArticleVO> toArticleVO(Map<Long, String> categoryNames) {
        return article -> {
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCategoryName(categoryNames.getOrDefault(article.getCategoryId(), null));
            vo.setPublishedAt(article.getPublishedAt());
            vo.setReadingMinutes(article.getReadingMinutes());
            return vo;
        };
    }

    private Page<ArticleVO> emptyPage(long page, long size) {
        Page<ArticleVO> empty = new Page<>(page, size);
        empty.setRecords(List.of());
        return empty;
    }
}
