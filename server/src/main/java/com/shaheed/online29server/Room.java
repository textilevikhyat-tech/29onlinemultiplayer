package com.shaheed.online29server;

import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final String id;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final GameEngine29 engine;
    private final Gson gson = new Gson();

    public Room(String id) {
        this.id = id;
        this.engine = new GameEngine29(this);
    }

    public String getId() { return id; }

    public void addPlayer(Player p) {
        if (players.size() < 4) players.add(p);
        broadcast(Map.of("type","player_joined","player", Map.of("id",p.getId(),"name",p.getName())));
        if (players.size() == 4) startGame();
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
        broadcast(Map.of("type","player_left","playerId",playerId));
    }

    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }

    public void startGame() {
        // engine deals, sets state
        engine.startRound(players);
        broadcastState();
    }

    public void handleAction(Player p, Map<String,Object> action) {
        engine.handleAction(p, action);
    }

    public void broadcast(Object o) {
        String json = gson.toJson(o);
        for (Player p : players) {
            try {
                if (p.isBot()) continue;
                if (p.getSession() != null && p.getSession().isOpen())
                    p.getSession().sendMessage(new org.springframework.web.socket.TextMessage(json));
            } catch (Exception e){ e.printStackTrace(); }
        }
    }

    public void broadcastState() {
        Map<String,Object> state = engine.getPublicState();
        state.put("type","state");
        state.put("roomId", id);
        // hide other players' hands: only include hand sizes and own hand for connected players
        List<Map<String,Object>> playersState = new ArrayList<>();
        for (Player p : players) {
            Map<String,Object> ps = new HashMap<>();
            ps.put("id", p.getId());
            ps.put("name", p.getName());
            ps.put("isBot", p.isBot());
            ps.put("handSize", engine.getHandSize(p.getId()));
            playersState.add(ps);
        }
        state.put("players", playersState);

        // send individual views (own hand) and broadcast
        String baseJson = gson.toJson(state);
        for (Player p : players) {
            try {
                Map<String,Object> personal = new HashMap<>(state);
                if (!p.isBot() && p.getSession()!=null && p.getSession().isOpen()) {
                    // include own hand array
                    personal.put("yourHand", engine.getHandForPlayer(p.getId()));
                    String personalJson = gson.toJson(personal);
                    p.getSession().sendMessage(new org.springframework.web.socket.TextMessage(personalJson));
                } else {
                    // bots: ignore
                    for (Player q: players) {
                        if (!q.isBot() && q.getSession()!=null && q.getSession().isOpen()) {
                            // nobody else should see full hands
                        }
                    }
                }
            } catch (Exception e){ e.printStackTrace(); }
        }

        // also broadcast a public state without hands for everyone (useful)
        broadcast(Map.of("type","public_state","roomId",id,"shortState",state));
    }
}
