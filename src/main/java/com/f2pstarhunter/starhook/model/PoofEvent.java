package com.f2pstarhunter.starhook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "poof_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoofEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer world;

    @Column(nullable = false)
    private Integer tier;  // What tier it was when it poofed

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Instant poofedAt;

    @Column(nullable = false)
    private Instant firstSeenAt;  // When the star was first spotted

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String source;

    public PoofEvent(ShootingStar star) {
        this.world = star.getWorld();
        this.tier = star.getTier();
        this.location = star.getLocation();
        this.poofedAt = Instant.now();
        this.firstSeenAt = star.getFirstSeenAt();
        this.type = star.getType();
        this.source = star.getSource();
    }
}