package com.mygame.server;

import spark.Spark;

public class Main {
    public static void main(String[] args) {

        Spark.port(8080);

        Spark.get("/", (req, res) -> {
            return "29 Multiplayer Server Running!";
        });

        Spark.get("/status", (req, res) -> {
            return "{ \"server\": \"online\", \"version\": 1 }";
        });
    }
}
