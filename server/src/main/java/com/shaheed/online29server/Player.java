package com.shaheed.online29server;

import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

public class Player {
    private final String id;
    private final String name;
    private final WebSocketSession session;
    private final boolean bot;

    public Player(String id, String name, WebSocketSession session, boolean bot) {
        this.id = id;
        this.name = name;
        this.session = session;
        this.bot = bot;
    }

    public static Player fromSession(WebSocketSession s, String name) {
        return new Player(UUID.randomUUID().toString(), name, s, false);
    }

    public static Player bot(String name) {
        return new Player("bot-" + UUID.randomUUID().toString(), name, null, true);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public WebSocketSession getSession() { return session; }
    public boolean isBot() { return bot; }
    public boolean isConnected() { return session != null && session.isOpen(); }
}
