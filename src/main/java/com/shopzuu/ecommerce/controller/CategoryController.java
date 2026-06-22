package com.shopzuu.ecommerce.controller;

import com.shopzuu.ecommerce.dto.response.ApiResponse;
import com.shopzuu.ecommerce.model.Category;
import com.shopzuu.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Categories", categoryRepository.findAll())
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(
            @RequestBody Category category) {
        return ResponseEntity.ok(
                ApiResponse.success("Category created",
                        categoryRepository.save(category))
        );
    }
}