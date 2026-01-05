package com.f2pstarhunter.starhook.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class StarLocationMapper {

    private static final Map<String, LocationCoordinates> LOCATION_MAP = new HashMap<>();

    static {
        LOCATION_MAP.put("akb", new LocationCoordinates(3276, 3164, 0));
        LOCATION_MAP.put("akm", new LocationCoordinates(3296, 3298, 0));
        LOCATION_MAP.put("apa", new LocationCoordinates(3351, 3281, 0));
        LOCATION_MAP.put("ccb", new LocationCoordinates(2567, 2858, 0));
        LOCATION_MAP.put("ccr", new LocationCoordinates(2483, 2886, 0));
        LOCATION_MAP.put("cg", new LocationCoordinates(2940, 3280, 0));
        LOCATION_MAP.put("dray", new LocationCoordinates(3094, 3235, 0));
        LOCATION_MAP.put("fb", new LocationCoordinates(3030, 3348, 0));
        LOCATION_MAP.put("ice", new LocationCoordinates(3018, 3444, 0));
        LOCATION_MAP.put("lse", new LocationCoordinates(3230, 3155, 0));
        LOCATION_MAP.put("lsw", new LocationCoordinates(3153, 3150, 0));
        LOCATION_MAP.put("nc", new LocationCoordinates(2835, 3296, 0));
        LOCATION_MAP.put("sc", new LocationCoordinates(2822, 3238, 0));
        LOCATION_MAP.put("vb", new LocationCoordinates(3258, 3408, 0));
        LOCATION_MAP.put("vse", new LocationCoordinates(3290, 3353, 0));
        LOCATION_MAP.put("vsw", new LocationCoordinates(3175, 3362, 0));
        LOCATION_MAP.put("rim", new LocationCoordinates(2974, 3241, 0));
    }

    public LocationCoordinates getCoordinates(String location) {
        LocationCoordinates coords = LOCATION_MAP.get(location.toLowerCase());
        if (coords == null) {
            throw new IllegalArgumentException("Unknown location: " + location);
        }
        return coords;
    }

    public static class LocationCoordinates {
        private final int x;
        private final int y;
        private final int plane;

        public LocationCoordinates(int x, int y, int plane) {
            this.x = x;
            this.y = y;
            this.plane = plane;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getPlane() { return plane; }
    }
}