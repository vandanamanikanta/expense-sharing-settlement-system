package com.example.expensesharing.dto;

import com.example.expensesharing.entity.Expense;
import com.example.expensesharing.entity.ExpenseSplit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExpenseResponseDto {
    private Long id;
    private String description;
    private BigDecimal amount;
    private UserResponseDto paidBy;
    private Long groupId;
    private LocalDateTime createdAt;
    private List<ExpenseSplitResponseDto> splits;

    @Data
    public static class ExpenseSplitResponseDto {
        private Long id;
        private UserResponseDto user;
        private BigDecimal shareAmount;

        public static ExpenseSplitResponseDto fromEntity(ExpenseSplit split) {
            ExpenseSplitResponseDto dto = new ExpenseSplitResponseDto();
            dto.setId(split.getId());
            dto.setUser(UserResponseDto.fromEntity(split.getUser()));
            dto.setShareAmount(split.getShareAmount());
            return dto;
        }
    }

    public static ExpenseResponseDto fromEntity(Expense expense) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setPaidBy(UserResponseDto.fromEntity(expense.getPaidBy()));
        dto.setGroupId(expense.getGroup().getId());
        dto.setCreatedAt(expense.getCreatedAt());

        if (expense.getSplits() != null) {
            dto.setSplits(expense.getSplits().stream()
                    .map(ExpenseSplitResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
