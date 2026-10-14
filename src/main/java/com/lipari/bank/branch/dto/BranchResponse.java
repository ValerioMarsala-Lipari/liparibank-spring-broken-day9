package com.lipari.bank.branch.dto;

public record BranchResponse(
        Long id,
        String name,
        String city
) {
}
