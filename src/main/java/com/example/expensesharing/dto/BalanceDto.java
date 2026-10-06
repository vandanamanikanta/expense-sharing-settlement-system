package com.example.expensesharing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for returning a single user's net balance within a group.
 * Positive balance = should receive money.
 * Negative balance = owes money.
 */
@Data
@AllArgsConstructor
public class BalanceDto {
    private Long userId;
    private String userName;
    private BigDecimal balance;
}
