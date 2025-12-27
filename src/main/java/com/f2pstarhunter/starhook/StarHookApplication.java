package com.f2pstarhunter.starhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StarHookApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarHookApplication.class, args);
    }

}
