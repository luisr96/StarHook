package com.f2pstarhunter.starhook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "scouting_locations")
public class ScoutingLocation {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer x;

    @Column(nullable = false)
    private Integer y;

    @Column(nullable = false)
    private Integer plane;

    @Column(nullable = false)
    private Integer radius;

    public ScoutingLocation() {}

    public ScoutingLocation(String id, String name, Integer x, Integer y, Integer plane, Integer radius) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoutingLocation that = (ScoutingLocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}