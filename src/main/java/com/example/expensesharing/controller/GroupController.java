package com.example.expensesharing.controller;

import com.example.expensesharing.dto.GroupRequestDto;
import com.example.expensesharing.dto.GroupResponseDto;
import com.example.expensesharing.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@Valid @RequestBody GroupRequestDto requestDto) {
        GroupResponseDto createdGroup = groupService.createGroup(requestDto);
        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDto> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponseDto> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupResponseDto updatedGroup = groupService.addMember(groupId, userId);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponseDto> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupResponseDto updatedGroup = groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(updatedGroup);
    }
}
