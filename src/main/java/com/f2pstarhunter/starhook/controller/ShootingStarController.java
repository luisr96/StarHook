package com.f2pstarhunter.starhook.controller;

import com.f2pstarhunter.starhook.service.ShootingStarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class ShootingStarController {

    private final ShootingStarService shootingStarService;

    public ShootingStarController(ShootingStarService shootingStarService) {
        this.shootingStarService = shootingStarService;
    }

    @PostMapping(value = "/stars", consumes = {"application/x-www-form-urlencoded", "multipart/form-data"})
    public ResponseEntity<Void> receiveWebhook(@RequestParam Map<String, String> allParams) {
        shootingStarService.processWebhook(allParams);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint is running");
    }
}