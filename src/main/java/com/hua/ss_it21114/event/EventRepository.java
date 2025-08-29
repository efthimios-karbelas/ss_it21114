package com.hua.ss_it21114.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = {"organization"})
    Page<Event> findByPublishedTrueAndStartsAtAfter(LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"organization"})
    Page<Event> findByOrganization_Id(Long orgId, Pageable pageable);

    @EntityGraph(attributePaths = {"organization"})
    List<Event> findTop100ByPublishedFalseOrderByStartsAtAsc();

    @EntityGraph(attributePaths = {"organization"})
    @Query("""
                select e
               from Event e
               where e.published = true
                  and (
                        (e.endsAt is null and e.startsAt >= :now)
                     or (e.endsAt >= :now)
                  )
                order by e.startsAt asc
            """)
    Page<Event> findPublishedActiveOrUpcoming(@Param("now") LocalDateTime now, Pageable pageable);
}
