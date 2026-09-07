package com.aitor.blog.article.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.aitor.blog.article.entity.ArticleCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface ArticleCategoryMapper extends BaseMapper<ArticleCategory> {
}
