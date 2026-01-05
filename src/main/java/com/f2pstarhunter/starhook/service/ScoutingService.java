package com.f2pstarhunter.starhook.service;

import com.f2pstarhunter.starhook.dto.ScoutingLocationDTO;
import com.f2pstarhunter.starhook.model.ParsedMessage;
import com.f2pstarhunter.starhook.model.ScoutingLocation;
import com.f2pstarhunter.starhook.model.ScoutingStatus;
import com.f2pstarhunter.starhook.repository.ScoutingLocationRepository;
import com.f2pstarhunter.starhook.repository.ScoutingStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoutingService {

    private static final Logger log = LoggerFactory.getLogger(ScoutingService.class);

    private final ScoutingLocationRepository locationRepository;
    private final ScoutingStatusRepository statusRepository;

    public ScoutingService(ScoutingLocationRepository locationRepository,
                           ScoutingStatusRepository statusRepository) {
        this.locationRepository = locationRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional
    public void handleScoutingClaim(ParsedMessage message, String scoutedBy) {
        String locationId = message.scoutingLocationId();

        // Verify location exists
        if (!locationRepository.existsById(locationId)) {
            throw new IllegalArgumentException("Invalid scouting location: " + locationId);
        }

        ScoutingStatus status = statusRepository.findById(locationId)
                .orElse(new ScoutingStatus(locationId, "not_scouted", null, null));

        if ("being_scouted".equals(status.getStatus()) && !scoutedBy.equals(status.getScoutedBy())) {
            log.warn("Location {} already being scouted by {}, but {} is trying to claim it",
                    locationId, status.getScoutedBy(), scoutedBy);
            throw new IllegalArgumentException("Location " + locationId + " is already being scouted by " + status.getScoutedBy());
        }

        status.markAsBeingScouted(scoutedBy);
        statusRepository.save(status);

        log.info("Location {} claimed by {}", locationId, scoutedBy);
    }

    @Transactional
    public void handleScoutingComplete(ParsedMessage message, String scoutedBy) {
        String locationId = message.scoutingLocationId();

        ScoutingStatus status = statusRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("No scouting status found for location: " + locationId));

        if (!scoutedBy.equals(status.getScoutedBy())) {
            log.warn("User {} trying to complete scouting for location {} claimed by {}",
                    scoutedBy, locationId, status.getScoutedBy());
            throw new IllegalArgumentException("You can only complete your own scouting claims");
        }

        status.markAsScouted();
        statusRepository.save(status);

        log.info("Location {} marked as scouted by {}", locationId, scoutedBy);
    }

    @Transactional
    public void handleWaveEnd() {
        log.info("Resetting all scouting locations for wave end");

        List<ScoutingStatus> allStatuses = statusRepository.findAll();
        allStatuses.forEach(ScoutingStatus::reset);
        statusRepository.saveAll(allStatuses);

        log.info("Reset {} scouting locations", allStatuses.size());
    }

    public List<ScoutingLocationDTO> getAllLocationsWithStatus() {
        List<ScoutingLocation> locations = locationRepository.findAll();

        return locations.stream()
                .map(location -> {
                    ScoutingStatus status = statusRepository.findById(location.getId())
                            .orElse(new ScoutingStatus(location.getId(), "not_scouted", null, null));

                    return new ScoutingLocationDTO(
                            location.getId(),
                            location.getName(),
                            location.getX(),
                            location.getY(),
                            location.getPlane(),
                            location.getRadius(),
                            status.getStatus(),
                            status.getScoutedBy(),
                            status.getClaimedAt(),
                            status.getCompletedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}