package com.lipari.bank.branch;

import com.lipari.bank.branch.dto.BranchResponse;
import com.lipari.bank.branch.dto.BranchStatsResponse;
import com.lipari.bank.branch.dto.BranchUpdateRequest;
import com.lipari.bank.branch.model.Branch;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final BranchRepository branchRepository;

    private final MeterRegistry meterRegistry;

    @Cacheable(value = "branches")
    public List<BranchResponse> findAllActive() {
        log.info("Cache miss — caricamento filiali attive dal database");
        return branchRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "branches", key = "#id")
    public BranchResponse findById(Long id) {
        log.info("Cache miss — caricamento filiale {} dal database", id);
        return branchRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Filiale non trovata: " + id));
    }

    @CachePut(value = "branches", key = "#branchId")
    public BranchResponse update(Long branchId, BranchUpdateRequest request) {
        log.info("Aggiornamento filiale {}", branchId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Filiale non trovata: " + branchId));
        branch.setName(request.name());
        branch.setCity(request.city());
        Branch saved = branchRepository.save(branch);
        return toResponse(saved);
    }

    @Caching(evict = {
        @CacheEvict(value = "branches", allEntries = true),
        @CacheEvict(value = "branchStats", allEntries = true)
    })
    public void deactivate(Long id) {
        log.info("Disattivazione filiale {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Filiale non trovata: " + id));
        branch.setActive(false);
        branchRepository.save(branch);
        meterRegistry.counter("branch.deactivations.total").increment();
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getCity());
    }

    @Cacheable(value = "branchStats")
    public BranchStatsResponse getStats() {
        log.info("Cache miss — calcolo statistiche filiali");

        List<Branch> branches = branchRepository.findAll();

        long totalBranches = branches.size();

        long activeBranches = branches.stream()
            .filter(Branch::isActive)
            .count();

        long inactiveBranches = totalBranches - activeBranches;

        long distinctActiveCities = branches.stream()
            .filter(Branch::isActive)
            .map(Branch::getCity)
            .distinct()
            .count();

        return new BranchStatsResponse(
            totalBranches,
            activeBranches,
            inactiveBranches,
            distinctActiveCities
        );
    }
}
