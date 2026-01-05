package com.f2pstarhunter.starhook.config;

import com.f2pstarhunter.starhook.model.ScoutingLocation;
import com.f2pstarhunter.starhook.repository.ScoutingLocationRepository;
import com.f2pstarhunter.starhook.repository.ScoutingStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ScoutingLocationInitializer implements CommandLineRunner {

    private final ScoutingLocationRepository locationRepository;

    public ScoutingLocationInitializer(ScoutingLocationRepository locationRepository,
                                       ScoutingStatusRepository statusRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public void run(String... args) {
        List<ScoutingLocation> locations = Arrays.asList(
                new ScoutingLocation("cg/rim", "cg/rim", 2958, 3265, 0, 32),
                new ScoutingLocation("akm/apa", "akm/apa", 3323, 3289, 0, 32),
                new ScoutingLocation("vb", "vb", 3275, 3384, 0, 32),
                new ScoutingLocation("vse", "vse", 3275, 3383, 0, 32),
                new ScoutingLocation("akb", "akb", 3248, 3186, 0, 32),
                new ScoutingLocation("lse", "lse", 3246, 3183, 0, 32),
                new ScoutingLocation("lsw", "lsw", 3153, 3150, 0, 32),
                new ScoutingLocation("ccr/mg", "ccr/mg", 2476, 2864, 0, 32),
                new ScoutingLocation("sc", "sc", 2822, 3238, 0, 32),
                new ScoutingLocation("nc", "nc", 2835, 3296, 0, 32),
                new ScoutingLocation("ice", "ice", 3018, 3444, 0, 32),
                new ScoutingLocation("fb", "fb", 3030, 3348, 0, 32),
                new ScoutingLocation("ccb", "ccb", 2567, 2858, 0, 32),
                new ScoutingLocation("dray", "dray", 3094, 3235, 0, 32),
                new ScoutingLocation("uzer", "uzer", 3400, 3173, 0, 32)
            );

        for (ScoutingLocation location : locations) {
            if (!locationRepository.existsById(location.getId())) {
                locationRepository.save(location);
            }
        }
    }
}