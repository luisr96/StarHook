package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.dto.ScoutingLocationDTO;
import com.f2pstarhunter.starhook.model.MessageType;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import com.f2pstarhunter.starhook.model.ScoutingLocation;
import com.f2pstarhunter.starhook.model.ScoutingStatus;
import com.f2pstarhunter.starhook.repository.ScoutingLocationRepository;
import com.f2pstarhunter.starhook.repository.ScoutingStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScoutingServiceTest {

    @Autowired
    private ScoutingService scoutingService;

    @Autowired
    private ScoutingLocationRepository locationRepository;

    @Autowired
    private ScoutingStatusRepository statusRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        statusRepository.deleteAll();
        locationRepository.deleteAll();

        // Add test location
        ScoutingLocation location = new ScoutingLocation("cg/rim", "CG / Rim hopping location", 2958, 3265, 0, 32);
        locationRepository.save(location);
    }

    @Test
    void testClaimScoutingLocation() {
        ParsedMessage message = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim");

        scoutingService.handleScoutingClaim(message, "TestUser");

        ScoutingStatus status = statusRepository.findById("cg/rim").orElseThrow();
        assertEquals("being_scouted", status.getStatus());
        assertEquals("TestUser", status.getScoutedBy());
        assertNotNull(status.getClaimedAt());
        assertNull(status.getCompletedAt());
    }

    @Test
    void testCompleteScoutingLocation() {
        // First claim it
        ParsedMessage claimMessage = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim");
        scoutingService.handleScoutingClaim(claimMessage, "TestUser");

        // Then complete it
        ParsedMessage completeMessage = ParsedMessage.forScouting(MessageType.SCOUTING_COMPLETED, "cg/rim");
        scoutingService.handleScoutingComplete(completeMessage, "TestUser");

        ScoutingStatus status = statusRepository.findById("cg/rim").orElseThrow();
        assertEquals("scouted", status.getStatus());
        assertEquals("TestUser", status.getScoutedBy());
        assertNotNull(status.getClaimedAt());
        assertNotNull(status.getCompletedAt());
    }

    @Test
    void testCannotCompleteOtherUsersScoutingClaim() {
        // User1 claims location
        ParsedMessage claimMessage = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim");
        scoutingService.handleScoutingClaim(claimMessage, "User1");

        // User2 tries to complete it
        ParsedMessage completeMessage = ParsedMessage.forScouting(MessageType.SCOUTING_COMPLETED, "cg/rim");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            scoutingService.handleScoutingComplete(completeMessage, "User2");
        });

        assertTrue(exception.getMessage().contains("You can only complete your own scouting claims"));
    }

    @Test
    void testCannotClaimAlreadyClaimedLocation() {
        // User1 claims location
        ParsedMessage message = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim");
        scoutingService.handleScoutingClaim(message, "User1");

        // User2 tries to claim same location
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            scoutingService.handleScoutingClaim(message, "User2");
        });

        assertTrue(exception.getMessage().contains("already being scouted"));
    }

    @Test
    void testSameUserCanReclaimLocation() {
        // User claims location
        ParsedMessage message = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim");
        scoutingService.handleScoutingClaim(message, "TestUser");

        // Same user reclaims
        assertDoesNotThrow(() -> {
            scoutingService.handleScoutingClaim(message, "TestUser");
        });
    }

    @Test
    void testWaveEndResetsAllLocations() {
        // Create multiple locations
        locationRepository.save(new ScoutingLocation("akm/apa", "AKM / APA", 3323, 3289, 0, 32));
        locationRepository.save(new ScoutingLocation("vb", "VB", 3275, 3384, 0, 32));

        // Claim some locations
        scoutingService.handleScoutingClaim(
                ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim"), "User1");
        scoutingService.handleScoutingClaim(
                ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "akm/apa"), "User2");

        // Complete one
        scoutingService.handleScoutingComplete(
                ParsedMessage.forScouting(MessageType.SCOUTING_COMPLETED, "cg/rim"), "User1");

        // Wave end
        scoutingService.handleWaveEnd();

        // Check all are reset
        List<ScoutingStatus> allStatuses = statusRepository.findAll();
        for (ScoutingStatus status : allStatuses) {
            assertEquals("not_scouted", status.getStatus());
            assertNull(status.getScoutedBy());
            assertNull(status.getClaimedAt());
            assertNull(status.getCompletedAt());
        }
    }

    @Test
    void testGetAllLocationsWithStatus() {
        // Add more locations
        locationRepository.save(new ScoutingLocation("akm/apa", "AKM / APA", 3323, 3289, 0, 32));
        locationRepository.save(new ScoutingLocation("vb", "VB", 3275, 3384, 0, 32));

        // Claim one location
        scoutingService.handleScoutingClaim(
                ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "cg/rim"), "TestUser");

        List<ScoutingLocationDTO> locations = scoutingService.getAllLocationsWithStatus();

        assertEquals(3, locations.size());

        // Check claimed location
        ScoutingLocationDTO claimed = locations.stream()
                .filter(l -> l.id().equals("cg/rim"))
                .findFirst()
                .orElseThrow();
        assertEquals("being_scouted", claimed.status());
        assertEquals("TestUser", claimed.scoutedBy());

        // Check unclaimed locations
        ScoutingLocationDTO unclaimed = locations.stream()
                .filter(l -> l.id().equals("akm/apa"))
                .findFirst()
                .orElseThrow();
        assertEquals("not_scouted", unclaimed.status());
        assertNull(unclaimed.scoutedBy());
    }

    @Test
    void testInvalidLocationThrowsException() {
        ParsedMessage message = ParsedMessage.forScouting(MessageType.SCOUTING_CLAIMED, "invalid-location");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            scoutingService.handleScoutingClaim(message, "TestUser");
        });

        assertTrue(exception.getMessage().contains("Invalid scouting location"));
    }

    @Test
    void testCompleteWithoutClaimThrowsException() {
        ParsedMessage message = ParsedMessage.forScouting(MessageType.SCOUTING_COMPLETED, "cg/rim");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            scoutingService.handleScoutingComplete(message, "TestUser");
        });

        assertTrue(exception.getMessage().contains("No scouting status found"));
    }
}