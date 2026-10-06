package com.example.expensesharing.service;

import com.example.expensesharing.dto.BalanceDto;
import com.example.expensesharing.entity.*;
import com.example.expensesharing.repository.ExpenseRepository;
import com.example.expensesharing.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupService groupService;
    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;

    /**
     * Calculates the net balance for every member in a group.
     *
     * Net balance = (amount paid for expenses)
     *             - (share owed in expenses)
     *             + (settlements paid TO others)    [reduces what you owe]
     *             - (settlements received FROM others)  [reduces what you are owed]
     *
     * Wait — let's think about this carefully:
     *   "payer" in a settlement is the person who pays back money they owe.
     *   So when Bob (payer) pays Alice (receiver) Rs 300:
     *     - Bob's debt decreases -> Bob's balance goes UP by 300
     *     - Alice's credit decreases -> Alice's balance goes DOWN by 300
     *
     * So: balance += settlements paid,  balance -= settlements received.
     */
    public List<BalanceDto> getBalancesForGroup(Long groupId) {
        ExpenseGroup group = groupService.getGroupEntityById(groupId);

        // Initialize balance map for every member to zero
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        for (User member : group.getMembers()) {
            balanceMap.put(member.getId(), BigDecimal.ZERO);
            nameMap.put(member.getId(), member.getName());
        }

        // Process expenses
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();

            // The payer paid the full amount -> their balance goes up
            balanceMap.merge(payerId, expense.getAmount(), BigDecimal::add);

            // Each participant owes their share -> their balance goes down
            for (ExpenseSplit split : expense.getSplits()) {
                Long userId = split.getUser().getId();
                balanceMap.merge(userId, split.getShareAmount().negate(), BigDecimal::add);
            }
        }

        // Process settlements
        List<Settlement> settlements = settlementRepository.findByGroupIdOrderBySettlementDateDesc(groupId);
        for (Settlement settlement : settlements) {
            Long payerId = settlement.getPayer().getId();
            Long receiverId = settlement.getReceiver().getId();

            // Payer paid back money they owed -> their balance goes up
            balanceMap.merge(payerId, settlement.getAmount(), BigDecimal::add);
            // Receiver got the money they were owed -> their balance goes down
            balanceMap.merge(receiverId, settlement.getAmount().negate(), BigDecimal::add);
        }

        // Convert map to sorted list of DTOs
        return balanceMap.entrySet().stream()
                .map(entry -> new BalanceDto(entry.getKey(), nameMap.get(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparing(BalanceDto::getUserId))
                .collect(Collectors.toList());
    }
}
