package com.example.expensesharing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupRequestDto {

    @NotBlank(message = "Group name is required")
    private String name;
}
