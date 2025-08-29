package com.hua.ss_it21114.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventSignupRepository extends JpaRepository<EventSignup, Long> {
    Optional<EventSignup> findByEvent_IdAndVolunteer_Id(Long eventId, Long volunteerId);

    @EntityGraph(attributePaths = {"event", "event.organization"})
    Page<EventSignup> findByEvent_Organization_IdAndStatus(Long orgId, EventSignup.Status status, Pageable pageable);

    @EntityGraph(attributePaths = {"event", "event.organization"})
    Page<EventSignup> findByVolunteer_Id(Long volunteerId, Pageable pageable);

    @Query("""
            select s
            from EventSignup s
              join fetch s.volunteer v
            where s.event.id = :eventId
            order by s.id asc
            """)
    List<EventSignup> findByEvent_IdOrderByIdAsc(@Param("eventId") Long eventId);

    Optional<EventSignup> findByIdAndVolunteer_Id(Long id, Long volunteerId);

    @Query("""
            select s.event.id
            from EventSignup s
            where s.volunteer.id = :userId
              and s.event.id in :eventIds
            """)
    Set<Long> findAppliedEventIdsByUserIdAndEventIds(
            @Param("userId") Long userId,
            @Param("eventIds") Collection<Long> eventIds
    );
}
