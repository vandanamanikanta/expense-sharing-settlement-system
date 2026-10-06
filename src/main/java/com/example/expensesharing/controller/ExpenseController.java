package com.example.expensesharing.controller;

import com.example.expensesharing.dto.ExpenseRequestDto;
import com.example.expensesharing.dto.ExpenseResponseDto;
import com.example.expensesharing.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/groups/{groupId}/expenses")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @PathVariable Long groupId, 
            @Valid @RequestBody ExpenseRequestDto requestDto) {
        ExpenseResponseDto createdExpense = expenseService.createExpense(groupId, requestDto);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroupId(groupId));
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(@PathVariable Long expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
