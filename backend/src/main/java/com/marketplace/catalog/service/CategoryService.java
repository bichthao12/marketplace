package com.marketplace.catalog.service;

import com.marketplace.catalog.dto.CategoryResponse;
import com.marketplace.catalog.entity.Category;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.CategoryRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree(UUID parentId, int depth) {
        List<Category> all = categoryRepository.findAll();
        Map<UUID, List<Category>> childrenByParent = all.stream()
                .filter(category -> category.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> roots = parentId == null
                ? all.stream().filter(category -> category.getParentId() == null).sorted(java.util.Comparator.comparingInt(Category::getSortOrder)).toList()
                : all.stream().filter(category -> parentId.equals(category.getParentId())).sorted(java.util.Comparator.comparingInt(Category::getSortOrder)).toList();

        return roots.stream()
                .map(root -> toTreeLimited(root, childrenByParent, depth))
                .toList();
    }

    private CategoryResponse toTreeLimited(Category category, Map<UUID, List<Category>> childrenByParent, int depth) {
        if (depth <= 1) {
            return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getImageUrl(), List.of());
        }
        return CatalogMapper.toCategoryTree(category, childrenByParent);
    }
}
