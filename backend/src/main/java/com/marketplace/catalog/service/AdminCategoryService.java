package com.marketplace.catalog.service;

import com.marketplace.catalog.dto.CreateCategoryRequest;
import com.marketplace.catalog.dto.UpdateCategoryRequest;
import com.marketplace.catalog.entity.Category;
import com.marketplace.catalog.exception.CategoryNotFoundException;
import com.marketplace.catalog.repository.CategoryRepository;
import com.marketplace.catalog.util.SlugUtils;
import com.marketplace.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Category getById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    }

    @Transactional
    public Category create(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ApiException("SLUG_ALREADY_EXISTS", "Category slug already exists", HttpStatus.CONFLICT);
        }
        if (request.parentId() != null && categoryRepository.findById(request.parentId()).isEmpty()) {
            throw new CategoryNotFoundException();
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(SlugUtils.slugify(request.slug()));
        category.setParentId(request.parentId());
        category.setSortOrder(request.sortOrder());
        category.setImageUrl(request.imageUrl());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(UUID id, UpdateCategoryRequest request) {
        Category category = getById(id);
        if (!category.getSlug().equals(request.slug()) && categoryRepository.existsBySlug(request.slug())) {
            throw new ApiException("SLUG_ALREADY_EXISTS", "Category slug already exists", HttpStatus.CONFLICT);
        }
        category.setName(request.name().trim());
        category.setSlug(SlugUtils.slugify(request.slug()));
        category.setParentId(request.parentId());
        category.setSortOrder(request.sortOrder());
        category.setImageUrl(request.imageUrl());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = getById(id);
        if (categoryRepository.countByParentId(id) > 0) {
            throw new ApiException("CATEGORY_HAS_CHILDREN", "Cannot delete category with children", HttpStatus.CONFLICT);
        }
        categoryRepository.delete(category);
    }
}
