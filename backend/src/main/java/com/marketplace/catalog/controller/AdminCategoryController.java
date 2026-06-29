package com.marketplace.catalog.controller;

import com.marketplace.catalog.dto.CategoryResponse;
import com.marketplace.catalog.dto.CreateCategoryRequest;
import com.marketplace.catalog.dto.UpdateCategoryRequest;
import com.marketplace.catalog.entity.Category;
import com.marketplace.catalog.service.AdminCategoryService;
import com.marketplace.catalog.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;
    private final CategoryService categoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService, CategoryService categoryService) {
        this.adminCategoryService = adminCategoryService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public Map<String, List<CategoryResponse>> list() {
        return Map.of("data", categoryService.getCategoryTree(null, 10));
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        Category category = adminCategoryService.getById(id);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getImageUrl(), List.of());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = adminCategoryService.create(request);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getImageUrl(), List.of());
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = adminCategoryService.update(id, request);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getImageUrl(), List.of());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        adminCategoryService.delete(id);
    }
}
