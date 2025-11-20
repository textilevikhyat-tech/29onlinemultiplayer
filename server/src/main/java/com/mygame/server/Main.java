package com.mygame.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Main {

    @GetMapping("/")
    public String home() {
        return "29 Multiplayer Server Running!";
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
