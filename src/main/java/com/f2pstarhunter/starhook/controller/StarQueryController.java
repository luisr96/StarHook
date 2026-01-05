package com.f2pstarhunter.starhook.controller;
import com.f2pstarhunter.starhook.dto.ShootingStarDTO;
import com.f2pstarhunter.starhook.model.PoofEvent;
import com.f2pstarhunter.starhook.model.ShootingStar;
import com.f2pstarhunter.starhook.repository.PoofEventRepository;
import com.f2pstarhunter.starhook.repository.ShootingStarRepository;
import com.f2pstarhunter.starhook.service.StarLocationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stars")
public class StarQueryController {
    private final ShootingStarRepository starRepository;
    private final PoofEventRepository poofEventRepository;
    private final StarLocationMapper locationMapper;
    public StarQueryController(ShootingStarRepository starRepository, PoofEventRepository poofEventRepository, StarLocationMapper locationMapper) {
        this.starRepository = starRepository;
        this.poofEventRepository = poofEventRepository;
        this.locationMapper = locationMapper;
    }
    @GetMapping("/active")
    public ResponseEntity<List<ShootingStarDTO>> getActiveStars() {
        List<ShootingStar> stars = starRepository.findAll();
        List<ShootingStarDTO> dtos = stars.stream()
                .map(star -> {
                    StarLocationMapper.LocationCoordinates coords = locationMapper.getCoordinates(star.getLocation());
                    return ShootingStarDTO.fromEntity(star, coords.getX(), coords.getY(), coords.getPlane());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/active/world/{world}")
    public ResponseEntity<ShootingStar> getStarByWorld(@PathVariable Integer world) {
        return starRepository.findByWorld(world)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/active/recent")
    public ResponseEntity<List<ShootingStar>> getRecentStars() {
        return ResponseEntity.ok(starRepository.findTop10ByOrderByLastUpdatedAtDesc());
    }
    @GetMapping("/poof-events")
    public ResponseEntity<List<PoofEvent>> getPoofEvents() {
        return ResponseEntity.ok(poofEventRepository.findTop100ByOrderByPoofedAtDesc());
    }
    @GetMapping("/poof-events/recent")
    public ResponseEntity<List<PoofEvent>> getRecentPoofEvents(@RequestParam(defaultValue = "2") int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return ResponseEntity.ok(poofEventRepository.findByPoofedAtAfter(since));
    }
    @GetMapping("/poof-events/world/{world}")
    public ResponseEntity<List<PoofEvent>> getPoofEventsByWorld(@PathVariable Integer world) {
        return ResponseEntity.ok(poofEventRepository.findByWorld(world));
    }
    @GetMapping("/stats/lifespans")
    public ResponseEntity<List<Long>> getStarLifespans() {
        return ResponseEntity.ok(poofEventRepository.findAllLifespansInMinutes());
    }
}