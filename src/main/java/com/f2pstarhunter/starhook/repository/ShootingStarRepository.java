package com.f2pstarhunter.starhook.repository;

import com.f2pstarhunter.starhook.model.ShootingStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShootingStarRepository extends JpaRepository<ShootingStar, Long> {

    Optional<ShootingStar> findByWorld(Integer world);

    List<ShootingStar> findByLastUpdatedAtAfter(LocalDateTime dateTime);

    List<ShootingStar> findTop10ByOrderByLastUpdatedAtDesc();
}