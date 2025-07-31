package com.example.expensetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.expensetracker.entity.ExpenseEntity;
import com.example.expensetracker.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity,Long> {

    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);
    
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);


    @Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

   List<IncomeEntity> findByProfileAndDateBetweenAndNameContainingIgnoreCase(
    Long profileId, 
    LocalDate startDate,
    LocalDate endDate,
    String keyword,
    Sort sort
    );

    List<IncomeEntity>findByProfileIdAndDateBetween(
        Long profileId, 
        LocalDate startDate, 
        LocalDate endDate
        
    );

    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long id, LocalDate startDate,
            LocalDate endDate, String keyword, Sort sort);
    
}
