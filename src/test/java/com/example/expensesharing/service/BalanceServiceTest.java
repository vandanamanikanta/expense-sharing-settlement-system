package com.example.expensesharing.service;

import com.example.expensesharing.dto.BalanceDto;
import com.example.expensesharing.entity.*;
import com.example.expensesharing.repository.ExpenseRepository;
import com.example.expensesharing.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private GroupService groupService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private BalanceService balanceService;

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
    @DisplayName("Balance: Rs 900 dinner paid by Alice, split among 3 -> Alice +600, Bob -300, Charlie -300")
    void testBalances_AfterExpense() {
        // Arrange: Create an expense of Rs 900 paid by Alice, split equally
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("900.00"));
        expense.setPaidBy(alice);
        expense.setGroup(group);

        ExpenseSplit splitAlice = new ExpenseSplit(null, expense, alice, new BigDecimal("300.00"));
        ExpenseSplit splitBob = new ExpenseSplit(null, expense, bob, new BigDecimal("300.00"));
        ExpenseSplit splitCharlie = new ExpenseSplit(null, expense, charlie, new BigDecimal("300.00"));
        expense.setSplits(List.of(splitAlice, splitBob, splitCharlie));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(expenseRepository.findByGroupIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(expense));
        when(settlementRepository.findByGroupIdOrderBySettlementDateDesc(1L)).thenReturn(Collections.emptyList());

        // Act
        List<BalanceDto> balances = balanceService.getBalancesForGroup(1L);

        // Assert
        assertEquals(3, balances.size());
        BalanceDto aliceBalance = balances.stream().filter(b -> b.getUserId().equals(1L)).findFirst().orElseThrow();
        BalanceDto bobBalance = balances.stream().filter(b -> b.getUserId().equals(2L)).findFirst().orElseThrow();
        BalanceDto charlieBalance = balances.stream().filter(b -> b.getUserId().equals(3L)).findFirst().orElseThrow();

        assertEquals(new BigDecimal("600.00"), aliceBalance.getBalance());
        assertEquals(new BigDecimal("-300.00"), bobBalance.getBalance());
        assertEquals(new BigDecimal("-300.00"), charlieBalance.getBalance());
    }

    @Test
    @DisplayName("Balance after settlement: Bob pays Alice Rs 300 -> Bob becomes 0, Alice becomes +300")
    void testBalances_AfterSettlement() {
        // Arrange: Same expense as above
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("900.00"));
        expense.setPaidBy(alice);
        expense.setGroup(group);

        ExpenseSplit splitAlice = new ExpenseSplit(null, expense, alice, new BigDecimal("300.00"));
        ExpenseSplit splitBob = new ExpenseSplit(null, expense, bob, new BigDecimal("300.00"));
        ExpenseSplit splitCharlie = new ExpenseSplit(null, expense, charlie, new BigDecimal("300.00"));
        expense.setSplits(List.of(splitAlice, splitBob, splitCharlie));

        // Bob settles Rs 300 with Alice
        Settlement settlement = new Settlement();
        settlement.setId(1L);
        settlement.setGroup(group);
        settlement.setPayer(bob);
        settlement.setReceiver(alice);
        settlement.setAmount(new BigDecimal("300.00"));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(expenseRepository.findByGroupIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(expense));
        when(settlementRepository.findByGroupIdOrderBySettlementDateDesc(1L)).thenReturn(List.of(settlement));

        // Act
        List<BalanceDto> balances = balanceService.getBalancesForGroup(1L);

        // Assert
        BalanceDto aliceBalance = balances.stream().filter(b -> b.getUserId().equals(1L)).findFirst().orElseThrow();
        BalanceDto bobBalance = balances.stream().filter(b -> b.getUserId().equals(2L)).findFirst().orElseThrow();
        BalanceDto charlieBalance = balances.stream().filter(b -> b.getUserId().equals(3L)).findFirst().orElseThrow();

        // Alice was +600, received 300 from Bob -> now +300
        assertEquals(new BigDecimal("300.00"), aliceBalance.getBalance());
        // Bob was -300, paid 300 to Alice -> now 0
        assertEquals(0, bobBalance.getBalance().compareTo(BigDecimal.ZERO));
        // Charlie still owes
        assertEquals(new BigDecimal("-300.00"), charlieBalance.getBalance());
    }

    @Test
    @DisplayName("All balances sum to zero (conservation of money)")
    void testBalances_SumToZero() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("900.00"));
        expense.setPaidBy(alice);
        expense.setGroup(group);

        ExpenseSplit splitAlice = new ExpenseSplit(null, expense, alice, new BigDecimal("300.00"));
        ExpenseSplit splitBob = new ExpenseSplit(null, expense, bob, new BigDecimal("300.00"));
        ExpenseSplit splitCharlie = new ExpenseSplit(null, expense, charlie, new BigDecimal("300.00"));
        expense.setSplits(List.of(splitAlice, splitBob, splitCharlie));

        when(groupService.getGroupEntityById(1L)).thenReturn(group);
        when(expenseRepository.findByGroupIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(expense));
        when(settlementRepository.findByGroupIdOrderBySettlementDateDesc(1L)).thenReturn(Collections.emptyList());

        List<BalanceDto> balances = balanceService.getBalancesForGroup(1L);

        BigDecimal sum = balances.stream()
                .map(BalanceDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, sum.compareTo(BigDecimal.ZERO), "All balances must sum to zero");
    }
}
