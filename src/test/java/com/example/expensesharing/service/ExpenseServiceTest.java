package com.example.expensesharing.service;

import com.example.expensesharing.dto.ExpenseRequestDto;
import com.example.expensesharing.entity.Expense;
import com.example.expensesharing.entity.ExpenseGroup;
import com.example.expensesharing.entity.User;
import com.example.expensesharing.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExpenseService expenseService;

    private User alice, bob, charlie;
    private ExpenseGroup group;

    @BeforeEach
    void setUp() {
        alice = new User(1L, "Alice", "alice@example.com");
        bob = new User(2L, "Bob", "bob@example.com");
        charlie = new User(3L, "Charlie", "charlie@example.com");

        group = new ExpenseGroup();
        group.setId(1L);
        group.setName("Friends Dinner");
        group.setMembers(new HashSet<>(Arrays.asList(alice, bob, charlie)));
    }

    @Test
    @DisplayName("Equal split: Rs 900 among 3 people = Rs 300 each")
    void testEqualSplit_EvenDivision() {
        // Arrange
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("Dinner");
        requestDto.setAmount(new BigDecimal("900.00"));
        requestDto.setPaidByUserId(1L);
        requestDto.setParticipantIds(List.of(1L, 2L, 3L));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(userService.getUserEntityById(1L)).thenReturn(alice);
        when(userService.getUserEntityById(2L)).thenReturn(bob);
        when(userService.getUserEntityById(3L)).thenReturn(charlie);

        // Capture the Expense object passed to save() so we can inspect its splits
        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        when(expenseRepository.save(captor.capture())).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        expenseService.createExpense(1L, requestDto);

        // Assert
        Expense savedExpense = captor.getValue();
        assertEquals(3, savedExpense.getSplits().size());

        BigDecimal totalSplits = BigDecimal.ZERO;
        for (var split : savedExpense.getSplits()) {
            assertEquals(new BigDecimal("300.00"), split.getShareAmount());
            totalSplits = totalSplits.add(split.getShareAmount());
        }
        // Total of all splits must equal the original amount
        assertEquals(new BigDecimal("900.00"), totalSplits);
    }

    @Test
    @DisplayName("Uneven split: Rs 100 among 3 people = 33.34 + 33.33 + 33.33")
    void testEqualSplit_UnevenDivision() {
        // Arrange
        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("Snacks");
        requestDto.setAmount(new BigDecimal("100.00"));
        requestDto.setPaidByUserId(1L);
        requestDto.setParticipantIds(List.of(1L, 2L, 3L));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(userService.getUserEntityById(1L)).thenReturn(alice);
        when(userService.getUserEntityById(2L)).thenReturn(bob);
        when(userService.getUserEntityById(3L)).thenReturn(charlie);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        when(expenseRepository.save(captor.capture())).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        expenseService.createExpense(1L, requestDto);

        // Assert
        Expense savedExpense = captor.getValue();
        assertEquals(3, savedExpense.getSplits().size());

        // First participant gets the extra penny: 33.34
        assertEquals(new BigDecimal("33.34"), savedExpense.getSplits().get(0).getShareAmount());
        // Remaining participants: 33.33 each
        assertEquals(new BigDecimal("33.33"), savedExpense.getSplits().get(1).getShareAmount());
        assertEquals(new BigDecimal("33.33"), savedExpense.getSplits().get(2).getShareAmount());

        // Total must add up exactly to 100.00
        BigDecimal totalSplits = savedExpense.getSplits().stream()
                .map(s -> s.getShareAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), totalSplits);
    }

    @Test
    @DisplayName("Non-member payer is rejected with IllegalArgumentException")
    void testCreateExpense_PayerNotMember() {
        User outsider = new User(99L, "Dave", "dave@example.com");

        ExpenseRequestDto requestDto = new ExpenseRequestDto();
        requestDto.setDescription("Lunch");
        requestDto.setAmount(new BigDecimal("300.00"));
        requestDto.setPaidByUserId(99L);
        requestDto.setParticipantIds(List.of(1L, 2L));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(userService.getUserEntityById(99L)).thenReturn(outsider);

        assertThrows(IllegalArgumentException.class, () -> {
            expenseService.createExpense(1L, requestDto);
        });
    }
}
