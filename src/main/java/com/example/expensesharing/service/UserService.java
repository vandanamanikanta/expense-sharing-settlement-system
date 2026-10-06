package com.example.expensesharing.service;

import com.example.expensesharing.dto.UserRequestDto;
import com.example.expensesharing.dto.UserResponseDto;
import com.example.expensesharing.entity.User;
import com.example.expensesharing.exception.ResourceConflictException;
import com.example.expensesharing.exception.UserNotFoundException;
import com.example.expensesharing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResourceConflictException("Email already in use");
        }

        User user = new User();
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        User savedUser = userRepository.save(user);
        return UserResponseDto.fromEntity(savedUser);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponseDto::fromEntity)
            .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return UserResponseDto.fromEntity(user);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User user = getUserEntityById(id);

        if (!user.getEmail().equals(requestDto.getEmail()) && userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResourceConflictException("Email already in use");
        }

        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        User updatedUser = userRepository.save(user);
        return UserResponseDto.fromEntity(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    // Helper method used internally (e.g. later when adding members to a group)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
