package com.lipari.bank.shared.config;

import com.lipari.bank.branch.BranchRepository;
import com.lipari.bank.branch.model.Branch;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final BranchRepository branchRepository;

    @PostConstruct
    public void init() {
        if (branchRepository.count() == 0) {
            branchRepository.save(Branch.builder()
                    .name("Filiale Palermo Centro")
                    .city("Palermo")
                    .active(true)
                    .build());
            branchRepository.save(Branch.builder()
                    .name("Filiale Milano Duomo")
                    .city("Milano")
                    .active(true)
                    .build());
            branchRepository.save(Branch.builder()
                    .name("Filiale Roma Termini")
                    .city("Roma")
                    .active(true)
                    .build());
            log.info("Dati iniziali caricati: 3 filiali");
        }
    }
}
