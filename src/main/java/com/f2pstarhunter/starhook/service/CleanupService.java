package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.repository.PoofEventRepository;
import com.f2pstarhunter.starhook.repository.ShootingStarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    @Value("${cleanup.star.expiry-minutes:93}")
    private int starExpiryMinutes;

    @Value("${cleanup.poof-event.expiry-hours:24}")
    private int poofEventExpiryHours;

    private final ShootingStarRepository starRepository;
    private final PoofEventRepository poofEventRepository;

    public CleanupService(ShootingStarRepository starRepository, PoofEventRepository poofEventRepository) {
        this.starRepository = starRepository;
        this.poofEventRepository = poofEventRepository;
    }

    @Scheduled(fixedRateString = "${cleanup.star.schedule-rate:300000}")
    @Transactional
    public void cleanupExpiredStars() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(starExpiryMinutes);

        int deletedCount = starRepository.deleteByFirstSeenAtBefore(expiryTime);

        if (deletedCount > 0) {
            log.info("Cleaned up {} expired stars (older than {} minutes)", deletedCount, starExpiryMinutes);
        }
    }

    @Scheduled(fixedRateString = "${cleanup.poof-event.schedule-rate:3600000}")
    @Transactional
    public void cleanupOldPoofEvents() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(poofEventExpiryHours);

        int deletedCount = poofEventRepository.deleteByPoofedAtBefore(expiryTime);

        if (deletedCount > 0) {
            log.info("Cleaned up {} old poof events (older than {} hours)", deletedCount, poofEventExpiryHours);
        }
    }
}