package com.example.expensesharing.service;

import com.example.expensesharing.dto.GroupRequestDto;
import com.example.expensesharing.dto.GroupResponseDto;
import com.example.expensesharing.entity.ExpenseGroup;
import com.example.expensesharing.entity.User;
import com.example.expensesharing.exception.GroupNotFoundException;
import com.example.expensesharing.repository.ExpenseGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final ExpenseGroupRepository groupRepository;
    private final UserService userService;

    public GroupResponseDto createGroup(GroupRequestDto requestDto) {
        ExpenseGroup group = new ExpenseGroup();
        group.setName(requestDto.getName());
        
        ExpenseGroup savedGroup = groupRepository.save(group);
        return GroupResponseDto.fromEntity(savedGroup);
    }

    public List<GroupResponseDto> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(GroupResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public GroupResponseDto getGroupById(Long id) {
        ExpenseGroup group = getGroupEntityById(id);
        return GroupResponseDto.fromEntity(group);
    }

    public GroupResponseDto addMember(Long groupId, Long userId) {
        ExpenseGroup group = getGroupEntityById(groupId);
        User user = userService.getUserEntityById(userId);
        
        group.addMember(user);
        ExpenseGroup updatedGroup = groupRepository.save(group);
        return GroupResponseDto.fromEntity(updatedGroup);
    }

    public GroupResponseDto removeMember(Long groupId, Long userId) {
        ExpenseGroup group = getGroupEntityById(groupId);
        User user = userService.getUserEntityById(userId);
        
        group.removeMember(user);
        ExpenseGroup updatedGroup = groupRepository.save(group);
        return GroupResponseDto.fromEntity(updatedGroup);
    }

    // Helper method used internally
    public ExpenseGroup getGroupEntityById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
    }
}
