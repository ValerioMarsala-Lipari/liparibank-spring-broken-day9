package com.lipari.bank.branch.dto;

public record BranchStatsResponse(
    long totalBranches,
    long activeBranches,
    long inactiveBranches,
    long distinctActiveCities
) {
}
