package com.hua.ss_it21114.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByUserId(Long userId);

    List<Volunteer> findByApprovedFalse();

    List<Volunteer> findTop100ByApprovedFalseOrderByIdAsc();
}
