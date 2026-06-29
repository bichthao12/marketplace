package com.marketplace.catalog.controller;

import com.marketplace.catalog.dto.CategoryResponse;
import com.marketplace.catalog.service.CategoryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Map<String, List<CategoryResponse>> list(
            @RequestParam(required = false) UUID parentId,
            @RequestParam(defaultValue = "2") int depth
    ) {
        return Map.of("data", categoryService.getCategoryTree(parentId, depth));
    }
}
