package com.hua.ss_it21114.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByUserId(Long userId);

    List<Organization> findByApprovedFalse();

    List<Organization> findTop100ByApprovedFalseOrderByIdAsc();
}
