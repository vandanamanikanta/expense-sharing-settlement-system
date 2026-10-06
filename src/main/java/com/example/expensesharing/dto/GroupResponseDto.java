package com.example.expensesharing.dto;

import com.example.expensesharing.entity.ExpenseGroup;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class GroupResponseDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private List<UserResponseDto> members;

    public static GroupResponseDto fromEntity(ExpenseGroup group) {
        GroupResponseDto dto = new GroupResponseDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedAt(group.getCreatedAt());
        
        if (group.getMembers() != null) {
            dto.setMembers(group.getMembers().stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
