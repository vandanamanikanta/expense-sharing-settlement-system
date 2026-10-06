package com.example.expensesharing.config;

import com.example.expensesharing.entity.*;
import com.example.expensesharing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Loads sample data on application startup for quick testing.
 * Creates users Alice, Bob, Charlie; a group "Friends Dinner";
 * adds all three as members; and creates a Rs 900 "Dinner" expense paid by Alice.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseGroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public void run(String... args) {
        // Only load sample data if the database is empty
        if (userRepository.count() > 0) {
            log.info("Sample data already exists, skipping initialization.");
            return;
        }

        log.info("Loading sample data...");

        // Create users
        User alice = new User(null, "Alice", "alice@example.com");
        User bob = new User(null, "Bob", "bob@example.com");
        User charlie = new User(null, "Charlie", "charlie@example.com");
        userRepository.saveAll(List.of(alice, bob, charlie));

        // Create group
        ExpenseGroup group = new ExpenseGroup();
        group.setName("Friends Dinner");
        group.addMember(alice);
        group.addMember(bob);
        group.addMember(charlie);
        groupRepository.save(group);

        // Create expense: Dinner Rs 900 paid by Alice, split among all three
        Expense expense = new Expense();
        expense.setDescription("Dinner");
        expense.setAmount(new BigDecimal("900.00"));
        expense.setPaidBy(alice);
        expense.setGroup(group);

        // Equal split: 900 / 3 = 300.00 each (divides evenly)
        BigDecimal share = new BigDecimal("900.00").divide(BigDecimal.valueOf(3), 2, RoundingMode.DOWN);
        for (User user : List.of(alice, bob, charlie)) {
            ExpenseSplit split = new ExpenseSplit();
            split.setUser(user);
            split.setShareAmount(share);
            expense.addSplit(split);
        }

        expenseRepository.save(expense);

        log.info("Sample data loaded: 3 users, 1 group, 1 expense (Rs 900 Dinner).");
    }
}
