package com.example.expensesharing.exception;

public class GroupNotFoundException extends ResourceNotFoundException {
    public GroupNotFoundException(Long id) {
        super("Group not found with id: " + id);
    }
}
