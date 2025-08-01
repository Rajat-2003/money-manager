package com.example.expensetracker.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.expensetracker.dto.CategoryDTO;
import com.example.expensetracker.entity.CategoryEntity;
import com.example.expensetracker.entity.ProfileEntity;
import com.example.expensetracker.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    

    //save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {

        ProfileEntity profile= profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profile.getId() ))
        {
            throw new RuntimeException("Category with this name already exists");

        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile);
        categoryRepository.save(newCategory);
        return toDTO(newCategory);

    }

    public List<CategoryDTO> getCategoriesForCurrentUser()
    {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream()
            .map(this::toDTO)
            .toList();
    }
    // get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return entities.stream()
            .map(this::toDTO)
            .toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto)
    {
        ProfileEntity profile =  profileService.getCurrentProfile(); // Ensure user is authenticated
        CategoryEntity existingCategory =categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
        .orElseThrow(()->new RuntimeException("Category not found"));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory= categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }

    
    // helper methods 
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile)
    {
        return CategoryEntity.builder()
            .name(categoryDTO.getName())
            .icon(categoryDTO.getIcon())
            .profile(profile)
            .type(categoryDTO.getType())
            .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .icon(entity.getIcon())
            .type(entity.getType())
            .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

}
