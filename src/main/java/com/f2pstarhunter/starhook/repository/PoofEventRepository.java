package com.f2pstarhunter.starhook.repository;

import com.f2pstarhunter.starhook.model.PoofEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PoofEventRepository extends JpaRepository<PoofEvent, Long> {

    // Find all poof events for a specific world
    List<PoofEvent> findByWorld(Integer world);

    // Find recent poof events ordered by time
    List<PoofEvent> findTop100ByOrderByPoofedAtDesc();

    // Find poof events after a certain time
    List<PoofEvent> findByPoofedAtAfter(LocalDateTime dateTime);

    // Get poof events within a time range
    List<PoofEvent> findByPoofedAtBetween(LocalDateTime start, LocalDateTime end);
}