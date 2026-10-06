package com.example.expensesharing.service;

import com.example.expensesharing.dto.SettlementRequestDto;
import com.example.expensesharing.dto.SettlementResponseDto;
import com.example.expensesharing.entity.ExpenseGroup;
import com.example.expensesharing.entity.Settlement;
import com.example.expensesharing.entity.User;
import com.example.expensesharing.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupService groupService;
    private final UserService userService;

    public SettlementResponseDto createSettlement(Long groupId, SettlementRequestDto requestDto) {
        ExpenseGroup group = groupService.getGroupEntityById(groupId);
        User payer = userService.getUserEntityById(requestDto.getPayerId());
        User receiver = userService.getUserEntityById(requestDto.getReceiverId());

        if (!group.getMembers().contains(payer)) {
            throw new IllegalArgumentException("Payer must be a member of the group");
        }
        if (!group.getMembers().contains(receiver)) {
            throw new IllegalArgumentException("Receiver must be a member of the group");
        }
        if (payer.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Payer and receiver cannot be the same person");
        }

        Settlement settlement = new Settlement();
        settlement.setGroup(group);
        settlement.setPayer(payer);
        settlement.setReceiver(receiver);
        settlement.setAmount(requestDto.getAmount());

        Settlement savedSettlement = settlementRepository.save(settlement);
        return SettlementResponseDto.fromEntity(savedSettlement);
    }

    public List<SettlementResponseDto> getSettlementsByGroupId(Long groupId) {
        // Validate group exists
        groupService.getGroupEntityById(groupId);

        return settlementRepository.findByGroupIdOrderBySettlementDateDesc(groupId).stream()
                .map(SettlementResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
