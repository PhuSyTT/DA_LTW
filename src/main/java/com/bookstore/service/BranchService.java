package com.bookstore.service;

import com.bookstore.entity.Branch;

import java.util.List;

public interface BranchService {
    List<Branch> getAllActiveBranches();
    Branch getBranchById(Long id);
}
