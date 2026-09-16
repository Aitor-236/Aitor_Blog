package com.aitor.blog.article.service;

import com.aitor.blog.article.dto.CategoryVO;

/**
 * 后台分类管理。service 层只返回业务对象或抛业务异常，
 * 统一由 controller 包装成 Result 返回前端。
 */
public interface AdminCategoryService {

    /** 新建分类，返回创建后的分类。 */
    CategoryVO createCategory(String categoryName, String categoryIdentifier);

    /** 修改分类名称/英文标识，留空表示不修改该项。 */
    CategoryVO updateCategory(Long categoryId, String categoryName, String categoryIdentifier);

    /** 删除分类，分类下仍有文章时抛业务异常。 */
    void deleteCategory(Long categoryId);
}
