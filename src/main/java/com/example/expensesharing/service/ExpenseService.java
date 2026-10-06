package com.example.expensesharing.service;

import com.example.expensesharing.dto.ExpenseRequestDto;
import com.example.expensesharing.dto.ExpenseResponseDto;
import com.example.expensesharing.entity.Expense;
import com.example.expensesharing.entity.ExpenseGroup;
import com.example.expensesharing.entity.ExpenseSplit;
import com.example.expensesharing.entity.User;
import com.example.expensesharing.exception.ExpenseNotFoundException;
import com.example.expensesharing.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;
    private final UserService userService;

    public ExpenseResponseDto createExpense(Long groupId, ExpenseRequestDto requestDto) {
        ExpenseGroup group = groupService.getGroupEntityById(groupId);
        User payer = userService.getUserEntityById(requestDto.getPaidByUserId());

        if (!group.getMembers().contains(payer)) {
            throw new IllegalArgumentException("Payer must be a member of the group");
        }

        List<User> participants = requestDto.getParticipantIds().stream()
                .map(userService::getUserEntityById)
                .collect(Collectors.toList());

        for (User participant : participants) {
            if (!group.getMembers().contains(participant)) {
                throw new IllegalArgumentException("Participant " + participant.getName() + " is not a member of the group");
            }
        }

        Expense expense = new Expense();
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setGroup(group);
        expense.setPaidBy(payer);

        calculateAndAddSplits(expense, participants);

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponseDto.fromEntity(savedExpense);
    }

    private void calculateAndAddSplits(Expense expense, List<User> participants) {
        int numParticipants = participants.size();
        BigDecimal amount = expense.getAmount();

        // Calculate base share per person
        BigDecimal baseShare = amount.divide(BigDecimal.valueOf(numParticipants), 2, RoundingMode.DOWN);
        
        // Calculate how much is left over (e.g. 100 / 3 = 33.33 * 3 = 99.99, remainder = 0.01)
        BigDecimal totalBase = baseShare.multiply(BigDecimal.valueOf(numParticipants));
        BigDecimal remainder = amount.subtract(totalBase);

        // 1 cent / 1 paise
        BigDecimal ONE_CENT = new BigDecimal("0.01");

        for (User participant : participants) {
            ExpenseSplit split = new ExpenseSplit();
            split.setUser(participant);
            
            if (remainder.compareTo(BigDecimal.ZERO) > 0) {
                split.setShareAmount(baseShare.add(ONE_CENT));
                remainder = remainder.subtract(ONE_CENT);
            } else {
                split.setShareAmount(baseShare);
            }
            
            expense.addSplit(split);
        }
    }

    public List<ExpenseResponseDto> getExpensesByGroupId(Long groupId) {
        // Validate group exists
        groupService.getGroupEntityById(groupId);

        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(ExpenseResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ExpenseResponseDto getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        return ExpenseResponseDto.fromEntity(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expenseRepository.delete(expense);
    }
}
