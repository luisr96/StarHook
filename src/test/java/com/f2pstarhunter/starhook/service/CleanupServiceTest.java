package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.model.PoofEvent;
import com.f2pstarhunter.starhook.model.ShootingStar;
import com.f2pstarhunter.starhook.repository.PoofEventRepository;
import com.f2pstarhunter.starhook.repository.ShootingStarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CleanupServiceTest {

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private ShootingStarRepository starRepository;

    @Autowired
    private PoofEventRepository poofEventRepository;

    @BeforeEach
    void setUp() {
        starRepository.deleteAll();
        poofEventRepository.deleteAll();
    }

    @Test
    void testCleanupExpiredStars() {
        // Create a recent star (should not be deleted)
        ShootingStar recentStar = new ShootingStar("Type1", "Source1", 5, 444, "varrock");
        starRepository.save(recentStar);

        // Create an old star (should be deleted)
        ShootingStar oldStar = new ShootingStar("Type1", "Source1", 7, 301, "lumbridge");
        starRepository.save(oldStar);

        // Update the timestamp to simulate an old star
        oldStar.setFirstSeenAt(LocalDateTime.now().minusMinutes(100));
        starRepository.save(oldStar);

        assertEquals(2, starRepository.count());

        // Run cleanup
        cleanupService.cleanupExpiredStars();

        // Only recent star should remain
        assertEquals(1, starRepository.count());
        assertTrue(starRepository.findByWorld(444).isPresent());
        assertFalse(starRepository.findByWorld(301).isPresent());
    }

    @Test
    void testCleanupOldPoofEvents() {
        // Create a recent poof event (should not be deleted)
        PoofEvent recentPoof = new PoofEvent();
        recentPoof.setWorld(444);
        recentPoof.setTier(5);
        recentPoof.setLocation("varrock");
        recentPoof.setType("Type1");
        recentPoof.setSource("Source1");
        recentPoof.setFirstSeenAt(LocalDateTime.now().minusMinutes(60));
        recentPoof.setPoofedAt(LocalDateTime.now());
        poofEventRepository.save(recentPoof);

        // Create an old poof event (should be deleted)
        PoofEvent oldPoof = new PoofEvent();
        oldPoof.setWorld(301);
        oldPoof.setTier(7);
        oldPoof.setLocation("lumbridge");
        oldPoof.setType("Type1");
        oldPoof.setSource("Source1");
        oldPoof.setFirstSeenAt(LocalDateTime.now().minusHours(26));
        oldPoof.setPoofedAt(LocalDateTime.now().minusHours(25)); // 25 hours ago
        poofEventRepository.save(oldPoof);

        assertEquals(2, poofEventRepository.count());

        // Run cleanup
        cleanupService.cleanupOldPoofEvents();

        // Only recent poof event should remain
        assertEquals(1, poofEventRepository.count());
        assertFalse(poofEventRepository.findByWorld(444).isEmpty());
        assertTrue(poofEventRepository.findByWorld(301).isEmpty());
    }
}