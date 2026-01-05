package com.f2pstarhunter.starhook.dto;

import java.time.Instant;

public record ScoutingLocationDTO(
        String id,
        String name,
        Integer x,
        Integer y,
        Integer plane,
        Integer radius,
        String status,
        String scoutedBy,
        Instant claimedAt,
        Instant completedAt
) {
}