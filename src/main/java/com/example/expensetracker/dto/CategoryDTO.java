package com.example.expensetracker.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private Long profileId;
    private String icon;
    private String type; // income or expense
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
