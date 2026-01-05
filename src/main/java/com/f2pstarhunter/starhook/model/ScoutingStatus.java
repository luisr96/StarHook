package com.f2pstarhunter.starhook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "scouting_status")
public class ScoutingStatus {
    @Id
    private String locationId;

    @Column(nullable = false)
    private String status;

    @Column
    private String scoutedBy;

    @Column
    private Instant claimedAt;

    @Column
    private Instant completedAt;

    public ScoutingStatus() {}

    public ScoutingStatus(String locationId, String status, String scoutedBy, Instant claimedAt) {
        this.locationId = locationId;
        this.status = status;
        this.scoutedBy = scoutedBy;
        this.claimedAt = claimedAt;
    }

    public void markAsBeingScouted(String scoutedBy) {
        this.status = "being_scouted";
        this.scoutedBy = scoutedBy;
        this.claimedAt = Instant.now();
        this.completedAt = null;
    }

    public void markAsScouted() {
        this.status = "scouted";
        this.completedAt = Instant.now();
    }

    public void reset() {
        this.status = "not_scouted";
        this.scoutedBy = null;
        this.claimedAt = null;
        this.completedAt = null;
    }

}