package com.example.expensesharing.dto;

import com.example.expensesharing.entity.Settlement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementResponseDto {
    private Long id;
    private Long groupId;
    private UserResponseDto payer;
    private UserResponseDto receiver;
    private BigDecimal amount;
    private LocalDateTime settlementDate;

    public static SettlementResponseDto fromEntity(Settlement settlement) {
        SettlementResponseDto dto = new SettlementResponseDto();
        dto.setId(settlement.getId());
        dto.setGroupId(settlement.getGroup().getId());
        dto.setPayer(UserResponseDto.fromEntity(settlement.getPayer()));
        dto.setReceiver(UserResponseDto.fromEntity(settlement.getReceiver()));
        dto.setAmount(settlement.getAmount());
        dto.setSettlementDate(settlement.getSettlementDate());
        return dto;
    }
}
