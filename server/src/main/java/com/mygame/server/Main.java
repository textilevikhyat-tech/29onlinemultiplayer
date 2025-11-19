package com.mygame.server;

import spark.Spark;

public class Main {
    public static void main(String[] args) {
        // Use Render's PORT env if present, otherwise default 10000
        int port = 10000;
        try {
            String p = System.getenv("PORT");
            if (p != null && !p.isEmpty()) port = Integer.parseInt(p);
        } catch (Exception ignored) {}

        Spark.port(port);

        Spark.get("/", (req, res) -> "29 Multiplayer Server Running!");
        Spark.get("/status", (req, res) -> "{ \"server\": \"online\", \"version\": 1 }");
    }
}
