package com.f2pstarhunter.starhook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "shooting_stars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShootingStar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer world;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private Integer tier;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Instant firstSeenAt;

    @Column(nullable = false)
    private Instant lastUpdatedAt;

    @Column
    private Instant poofedAt;  // Only set when it randomly disappears

    public ShootingStar(String type, String source, Integer tier, Integer world, String location) {
        this.type = type;
        this.source = source;
        this.tier = tier;
        this.world = world;
        this.location = location;
        this.firstSeenAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }

    public void update(Integer newTier, String newLocation, String newType, String newSource) {
        if (newTier != null) this.tier = newTier;
        if (newLocation != null && !newLocation.isEmpty()) this.location = newLocation;
        this.type = newType;
        this.source = newSource;
        this.lastUpdatedAt = Instant.now();
    }
}