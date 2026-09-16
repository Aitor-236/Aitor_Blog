package com.aitor.blog.article.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aitor.blog.article.dto.CategoryVO;
import com.aitor.blog.article.entity.Article;
import com.aitor.blog.article.entity.ArticleCategory;
import com.aitor.blog.article.mapper.ArticleCategoryMapper;
import com.aitor.blog.article.mapper.ArticleMapper;
import com.aitor.blog.article.service.AdminCategoryService;
import com.aitor.blog.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    /** 新增分类时排序值的步长，保持与预置分类（10/20/30...）一致的间距。 */
    private static final int SORT_ORDER_STEP = 10;

    private final ArticleCategoryMapper articleCategoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public CategoryVO createCategory(String categoryName, String categoryIdentifier) {
        if (!StringUtils.hasText(categoryName) || !StringUtils.hasText(categoryIdentifier)) {
            throw new BusinessException("分类名称和英文标识均不能为空");
        }

        String name = categoryName.trim();
        String slug = categoryIdentifier.trim();
        if (existsSlug(slug, null)) {
            throw new BusinessException("分类英文标识已存在");
        }

        ArticleCategory category = new ArticleCategory();
        category.setName(name);
        category.setSlug(slug);
        category.setSortOrder(nextSortOrder());
        category.setCreatedAt(LocalDateTime.now());
        articleCategoryMapper.insert(category);

        return toCategoryVO(category, 0L);
    }

    @Override
    public CategoryVO updateCategory(Long categoryId, String categoryName, String categoryIdentifier) {
        if (categoryId == null) {
            throw new BusinessException("分类ID不能为空");
        }

        ArticleCategory existing = articleCategoryMapper.selectById(categoryId);
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }

        ArticleCategory category = new ArticleCategory();
        category.setId(existing.getId());
        if (StringUtils.hasText(categoryName)) {
            category.setName(categoryName.trim());
        }
        if (StringUtils.hasText(categoryIdentifier)) {
            String slug = categoryIdentifier.trim();
            if (existsSlug(slug, existing.getId())) {
                throw new BusinessException("分类英文标识已存在");
            }
            category.setSlug(slug);
        }
        articleCategoryMapper.updateById(category);

        return loadCategoryVO(existing.getId());
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (articleCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // article.category_id 是 RESTRICT 外键，先挡住占用中的分类，避免抛数据库异常。
        Long articleCount = countArticles(categoryId);
        if (articleCount > 0) {
            throw new BusinessException("该分类下还有 " + articleCount + " 篇文章，无法删除");
        }

        articleCategoryMapper.deleteById(categoryId);
    }

    /**
     * 判断英文标识是否已被其它分类占用，excludeId 用于更新时排除自身。
     */
    private boolean existsSlug(String slug, Long excludeId) {
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<ArticleCategory>()
                .eq(ArticleCategory::getSlug, slug);
        if (excludeId != null) {
            wrapper.ne(ArticleCategory::getId, excludeId);
        }
        return articleCategoryMapper.selectCount(wrapper) > 0;
    }

    /**
     * 新分类排在已有分类之后：取当前最大排序值加固定步长。
     */
    private Integer nextSortOrder() {
        ArticleCategory last = articleCategoryMapper.selectOne(
                new LambdaQueryWrapper<ArticleCategory>()
                        .orderByDesc(ArticleCategory::getSortOrder)
                        .orderByDesc(ArticleCategory::getId)
                        .last("limit 1"));
        if (last == null || last.getSortOrder() == null) {
            return SORT_ORDER_STEP;
        }
        return last.getSortOrder() + SORT_ORDER_STEP;
    }

    private Long countArticles(Long categoryId) {
        return articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, categoryId));
    }

    private CategoryVO loadCategoryVO(Long categoryId) {
        ArticleCategory category = articleCategoryMapper.selectById(categoryId);
        if (category == null) {
            return null;
        }
        return toCategoryVO(category, countArticles(categoryId));
    }

    private CategoryVO toCategoryVO(ArticleCategory category, Long articleCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSlug(category.getSlug());
        vo.setArticleCount(articleCount);
        return vo;
    }
}
