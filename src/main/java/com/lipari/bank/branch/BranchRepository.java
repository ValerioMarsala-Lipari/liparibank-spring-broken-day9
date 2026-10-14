package com.lipari.bank.branch;

import com.lipari.bank.branch.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findAllByActiveTrue();
}
