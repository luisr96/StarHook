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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShootingStarServiceIntegrationTest {

    @Autowired
    private ShootingStarService service;

    @Autowired
    private ShootingStarRepository repository;

    @Autowired
    private PoofEventRepository poofEventRepository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        poofEventRepository.deleteAll();
    }

    @Test
    void testProcessNewStar() {
        Map<String, String> params = createWebhookParams("T9 W455 ccb");

        service.processWebhook(params);

        Optional<ShootingStar> saved = repository.findByWorld(455);
        assertTrue(saved.isPresent());
        assertEquals(9, saved.get().getTier());
        assertEquals(455, saved.get().getWorld());
        assertEquals("ccb", saved.get().getLocation());
    }

    @Test
    void testProcessStarUpdate() {
        // Create new star
        Map<String, String> params1 = createWebhookParams("T8 W456 cg");
        service.processWebhook(params1);

        ShootingStar original = repository.findByWorld(456).orElseThrow();
        assertEquals(8, original.getTier());
        long originalId = original.getId();

        // Update tier
        Map<String, String> params2 = createWebhookParams("T7 W456");
        service.processWebhook(params2);

        ShootingStar updated = repository.findByWorld(456).orElseThrow();
        assertEquals(7, updated.getTier());
        assertEquals("cg", updated.getLocation()); // Location should remain
        assertEquals(originalId, updated.getId()); // Should be same record
    }

    @Test
    void testProcessStarUpdateWithNewLocation() {
        // Ability to overwrite locations in case of input error on first time
        // First message
        Map<String, String> params1 = createWebhookParams("T5 W456 vsw");
        service.processWebhook(params1);

        // Second message with new location
        Map<String, String> params2 = createWebhookParams("T5 W456 vse");
        service.processWebhook(params2);

        ShootingStar updated = repository.findByWorld(456).orElseThrow();
        assertEquals(5, updated.getTier());
        assertEquals("vse", updated.getLocation());
    }

    @Test
    void testProcessMultipleWorlds() {
        Map<String, String> params1 = createWebhookParams("T5 W444 vb");
        Map<String, String> params2 = createWebhookParams("T7 W301 lse");

        service.processWebhook(params1);
        service.processWebhook(params2);

        assertEquals(2, repository.count());
        assertTrue(repository.findByWorld(444).isPresent());
        assertTrue(repository.findByWorld(301).isPresent());
    }

    @Test
    void testProcessWorldFirstFormat() {
        Map<String, String> params = createWebhookParams("W444 T5 akm");

        service.processWebhook(params);

        Optional<ShootingStar> saved = repository.findByWorld(444);
        assertTrue(saved.isPresent());
        assertEquals(5, saved.get().getTier());
    }

    @Test
    void testProcessInvalidMessage() {
        Map<String, String> params = createWebhookParams("invalid message");

        assertThrows(RuntimeException.class, () -> {
            service.processWebhook(params);
        });

        assertEquals(0, repository.count());
    }

    @Test
    void testProcessUpdateWithoutInitialLocation() {
        // Trying to update a non-existent star without location should fail
        Map<String, String> params = createWebhookParams("T5 W444");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processWebhook(params);
        });

        assertTrue(exception.getMessage().contains("Location is required"));
    }

    @Test
    void testTierDepletion() {
        // Simulate a star depleting over time
        Map<String, String> params1 = createWebhookParams("T9 W444 vb");
        service.processWebhook(params1);
        assertEquals(9, repository.findByWorld(444).orElseThrow().getTier());

        Map<String, String> params2 = createWebhookParams("T7 W444");
        service.processWebhook(params2);
        assertEquals(7, repository.findByWorld(444).orElseThrow().getTier());

        Map<String, String> params3 = createWebhookParams("T5 W444");
        service.processWebhook(params3);
        assertEquals(5, repository.findByWorld(444).orElseThrow().getTier());

        Map<String, String> params4 = createWebhookParams("T2 W444");
        service.processWebhook(params4);
        assertEquals(2, repository.findByWorld(444).orElseThrow().getTier());

        // Still only one record
        assertEquals(1, repository.count());
    }

    @Test
    void testProcessStarDust() {
        // Create a star
        Map<String, String> params1 = createWebhookParams("T5 W444 apa");
        service.processWebhook(params1);
        assertTrue(repository.findByWorld(444).isPresent());

        // Mark as dusted
        Map<String, String> params2 = createWebhookParams("W444 dust");
        service.processWebhook(params2);

        // Should be deleted
        assertFalse(repository.findByWorld(444).isPresent());
    }

    @Test
    void testProcessStarPoof() {
        // Create a star first
        Map<String, String> params1 = createWebhookParams("T5 W444 apa");
        service.processWebhook(params1);
        assertTrue(repository.findByWorld(444).isPresent());

        // Mark as poofed
        Map<String, String> params2 = createWebhookParams("W444 poof");
        service.processWebhook(params2);

        // Should be deleted
        assertFalse(repository.findByWorld(444).isPresent());
    }

    @Test
    void testDepletionWithoutExistingStar() {
        // Trying to deplete a non-existent star should fail
        Map<String, String> params = createWebhookParams("W454 dust");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processWebhook(params);
        });

        assertTrue(exception.getMessage().contains("No star found on world 454"));
    }

    @Test
    void testDisappearanceWithoutExistingStar() {
        // Trying to mark disappeared a non-existent star should fail
        Map<String, String> params = createWebhookParams("W454 poof");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            service.processWebhook(params);
        });

        assertTrue(exception.getMessage().contains("No star found on world 454"));
    }

    @Test
    void testProcessStarDisappearanceRecordsPoofEvent() throws Exception {
        // Create a star first
        Map<String, String> params1 = createWebhookParams("T5 W444 vb");
        service.processWebhook(params1);

        ShootingStar star = repository.findByWorld(444).orElseThrow();
        LocalDateTime firstSeenAt = star.getFirstSeenAt();

        // Small delay to ensure different timestamp
        Thread.sleep(10);

        // Mark as disappeared
        Map<String, String> params2 = createWebhookParams("W444 poof");
        service.processWebhook(params2);

        // Star should be deleted from active tracking
        assertFalse(repository.findByWorld(444).isPresent());

        // But poof event should be recorded
        List<PoofEvent> poofEvents = poofEventRepository.findByWorld(444);
        assertEquals(1, poofEvents.size());

        PoofEvent event = poofEvents.get(0);
        assertEquals(444, event.getWorld());
        assertEquals(5, event.getTier());
        assertEquals("vb", event.getLocation());
        assertEquals(firstSeenAt, event.getFirstSeenAt());
        assertNotNull(event.getPoofedAt());
    }

    @Test
    void testMultiplePoofEventsRecorded() throws Exception {
        // Create and poof multiple stars
        service.processWebhook(createWebhookParams("T7 W420 dray"));
        service.processWebhook(createWebhookParams("420 poof"));

        service.processWebhook(createWebhookParams("T3 W301 lse"));
        service.processWebhook(createWebhookParams("W301 poof"));

        // Another star appears in the same world as another
        // It should be recorded as well instead of replaced
        service.processWebhook(createWebhookParams("T9 W420 ccb"));
        service.processWebhook(createWebhookParams("W420 poof"));

        // No active stars
        assertEquals(0, repository.count());

        // But 3 poof events recorded
        assertEquals(3, poofEventRepository.count());
    }

    private Map<String, String> createWebhookParams(String message) {
        Map<String, String> params = new HashMap<>();

        String payload = String.format(
                "{\"extra\":{\"type\":\"TradeFor200k\",\"source\":\"FRIENDSCHAT\",\"message\":\"%s\"}}",
                message
        );

        params.put("payload_json", payload);
        return params;
    }
}