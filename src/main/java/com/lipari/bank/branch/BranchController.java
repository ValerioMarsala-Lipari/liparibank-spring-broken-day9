package com.lipari.bank.branch;

import com.lipari.bank.branch.dto.BranchResponse;
import com.lipari.bank.branch.dto.BranchStatsResponse;
import com.lipari.bank.branch.dto.BranchUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Gestione filiali bancarie")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Recupera tutte le filiali attive")
    public ResponseEntity<List<BranchResponse>> getAllActive() {
        return ResponseEntity.ok(branchService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera una filiale per ID")
    public ResponseEntity<BranchResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna nome e città di una filiale")
    public ResponseEntity<BranchResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disattiva una filiale")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        branchService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Recupera le statistiche delle filiali")
    public ResponseEntity<BranchStatsResponse> getStats() {
        return ResponseEntity.ok(branchService.getStats());
    }
}
