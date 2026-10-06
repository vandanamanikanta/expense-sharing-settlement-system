package com.example.expensesharing.dto;

import com.example.expensesharing.entity.User;
import lombok.Data;

/**
 * DTO for returning user information.
 * It hides internal fields like passwords (if we had any) or other metadata
 * that the client doesn't need to see.
 */
@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;

    // Static factory method for mapping Entity to DTO
    public static UserResponseDto fromEntity(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
