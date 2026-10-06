package com.example.expensesharing.controller;

import com.example.expensesharing.dto.BalanceDto;
import com.example.expensesharing.dto.SettlementRequestDto;
import com.example.expensesharing.dto.SettlementResponseDto;
import com.example.expensesharing.service.BalanceService;
import com.example.expensesharing.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;
    private final BalanceService balanceService;

    @PostMapping("/settlements")
    public ResponseEntity<SettlementResponseDto> createSettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody SettlementRequestDto requestDto) {
        SettlementResponseDto settlement = settlementService.createSettlement(groupId, requestDto);
        return new ResponseEntity<>(settlement, HttpStatus.CREATED);
    }

    @GetMapping("/settlements")
    public ResponseEntity<List<SettlementResponseDto>> getSettlementsByGroupId(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlementsByGroupId(groupId));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<BalanceDto>> getBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.getBalancesForGroup(groupId));
    }
}
