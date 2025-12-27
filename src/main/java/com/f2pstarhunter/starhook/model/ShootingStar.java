package com.f2pstarhunter.starhook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private LocalDateTime firstSeenAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column
    private LocalDateTime poofedAt;  // Only set when it randomly disappears

    public ShootingStar(String type, String source, Integer tier, Integer world, String location) {
        this.type = type;
        this.source = source;
        this.tier = tier;
        this.world = world;
        this.location = location;
        this.firstSeenAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void update(Integer newTier, String newLocation, String newType, String newSource) {
        if (newTier != null) this.tier = newTier;
        if (newLocation != null && !newLocation.isEmpty()) this.location = newLocation;
        this.type = newType;
        this.source = newSource;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}