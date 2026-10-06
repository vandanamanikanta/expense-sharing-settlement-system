package com.example.expensesharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for creating or updating a user.
 * We use DTOs to decouple our internal database entities from the API contract,
 * ensuring clients only send/receive data we explicitly allow.
 */
@Data
public class UserRequestDto {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
