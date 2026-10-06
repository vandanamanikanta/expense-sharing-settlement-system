package com.example.expensesharing.exception;

public class ExpenseNotFoundException extends ResourceNotFoundException {
    public ExpenseNotFoundException(Long id) {
        super("Expense not found with id: " + id);
    }
}
