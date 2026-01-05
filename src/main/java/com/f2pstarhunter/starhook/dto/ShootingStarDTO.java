package com.f2pstarhunter.starhook.dto;

import com.f2pstarhunter.starhook.model.ShootingStar;

import java.time.Instant;

public record ShootingStarDTO(
        Long id,
        Integer world,
        String type,
        String source,
        Integer tier,
        String location,
        Integer x,
        Integer y,
        Integer plane,
        Instant firstSeenAt,
        Instant lastUpdatedAt,
        Instant poofedAt
) {
    public static ShootingStarDTO fromEntity(ShootingStar star, int x, int y, int plane) {
        return new ShootingStarDTO(
                star.getId(),
                star.getWorld(),
                star.getType(),
                star.getSource(),
                star.getTier(),
                star.getLocation(),
                x, y, plane,
                star.getFirstSeenAt(),
                star.getLastUpdatedAt(),
                star.getPoofedAt()
        );
    }
}