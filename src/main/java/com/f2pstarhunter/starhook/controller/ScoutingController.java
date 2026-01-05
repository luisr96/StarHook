package com.f2pstarhunter.starhook.controller;

import com.f2pstarhunter.starhook.dto.ScoutingLocationDTO;
import com.f2pstarhunter.starhook.service.ScoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scouting")
public class ScoutingController {

    private final ScoutingService scoutingService;

    public ScoutingController(ScoutingService scoutingService) {
        this.scoutingService = scoutingService;
    }

    @GetMapping("/locations")
    public ResponseEntity<List<ScoutingLocationDTO>> getAllLocations() {
        return ResponseEntity.ok(scoutingService.getAllLocationsWithStatus());
    }
}