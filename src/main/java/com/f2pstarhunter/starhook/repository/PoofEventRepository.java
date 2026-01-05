package com.f2pstarhunter.starhook.repository;

import com.f2pstarhunter.starhook.model.PoofEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.util.List;

@Repository
public interface PoofEventRepository extends JpaRepository<PoofEvent, Long> {

    // Find all poof events for a specific world
    List<PoofEvent> findByWorld(Integer world);

    // Find recent poof events ordered by time
    List<PoofEvent> findTop100ByOrderByPoofedAtDesc();

    // Find poof events after a certain time
    List<PoofEvent> findByPoofedAtAfter(Instant dateTime);

    // Get poof events within a time range
    List<PoofEvent> findByPoofedAtBetween(Instant start, Instant end);

    @Query("SELECT TIMESTAMPDIFF(MINUTE, p.firstSeenAt, p.poofedAt) FROM PoofEvent p")
    List<Long> findAllLifespansInMinutes();

    // Delete poof events older than the given time
    @Modifying
    @Query("DELETE FROM PoofEvent p WHERE p.poofedAt < :expiryTime")
    int deleteByPoofedAtBefore(@Param("expiryTime") Instant expiryTime);
}