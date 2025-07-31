package com.example.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.expensetracker.dto.ExpenseDTO;
import com.example.expensetracker.entity.CategoryEntity;
import com.example.expensetracker.entity.ExpenseEntity;
import com.example.expensetracker.entity.ProfileEntity;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;


    public ExpenseDTO addExpense(ExpenseDTO dto)
    {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category =categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Category not found"));
        ExpenseEntity newExpense=toEntity(dto, profile, category);
        newExpense=expenseRepository.save(newExpense);
        return toDTO(newExpense);



    }

    // retrieve expenses for current month on start date and end date 
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser()
    {
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(
            profile.getId(), startDate, endDate);

        return list.stream().map(this::toDTO).toList();

    }

    // delete expense by id for current user
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("You do not have permission to delete this expense");
        }
        expenseRepository.delete(entity);
    }

    // get latest 5 expenses for current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // get total expense for current user
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total= expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    // filter expenses
    public List<ExpenseDTO> filterExpenses( LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
            profile.getId(), startDate, endDate, keyword, sort);
            return list.stream().map(this::toDTO).toList();
    }

    // notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }
   

    // helper methods 
    private ExpenseEntity toEntity(ExpenseDTO dto , ProfileEntity profile ,CategoryEntity category)

    {
        return ExpenseEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .icon(dto.getIcon())
                .category(category)
                .amount(dto.getAmount())
                .date(dto.getDate())
                
                .profile(profile)
                .build();
        
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryName(entity.getCategory()!=null ? entity.getCategory().getName() : null)
                .categoryId(entity.getCategory()!= null ? entity.getCategory().getId() : null)
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
