package com.lipari.bank.branch.dto;

import jakarta.validation.constraints.NotBlank;

public record BranchUpdateRequest(
        @NotBlank String name,
        @NotBlank String city
) {
}
