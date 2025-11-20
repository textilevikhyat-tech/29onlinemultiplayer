package com.shaheed.online29server;

import com.google.gson.Gson;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.*;

public class RoomManager {
    private static final RoomManager INSTANCE = new RoomManager();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Queue<Player> quickQueue = new ConcurrentLinkedQueue<>();
    private final Gson gson = new Gson();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private RoomManager() {}

    public static RoomManager getInstance() { return INSTANCE; }

    public void registerSession(WebSocketSession s) {
        // placeholder
    }

    public void joinQuickMatch(WebSocketSession session, String name) {
        Player p = Player.fromSession(session, name);
        sessionToPlayer.put(session.getId(), p);
        quickQueue.add(p);
        tryMatchQueue();
        send(session, Map.of("type","queued"));
    }

    public void createRoom(WebSocketSession session, String name) {
        Player p = Player.fromSession(session, name);
        sessionToPlayer.put(session.getId(), p);
        String id = UUID.randomUUID().toString().substring(0,8);
        Room r = new Room(id);
        rooms.put(id, r);
        r.addPlayer(p);
        send(session, Map.of("type","room_created","roomId",id));
    }

    public void joinRoom(WebSocketSession session, String roomId, String name) {
        Room r = rooms.get(roomId);
        if (r == null) {
            send(session, Map.of("type","error","msg","room_not_found"));
            return;
        }
        Player p = Player.fromSession(session, name);
        sessionToPlayer.put(session.getId(), p);
        r.addPlayer(p);
    }

    public void handlePlayerAction(WebSocketSession session, Map<String,Object> msg) {
        Player p = sessionToPlayer.get(session.getId());
        if (p == null) {
            send(session, Map.of("type","error","msg","not_registered"));
            return;
        }
        String roomId = (String) msg.get("roomId");
        Room r = rooms.get(roomId);
        if (r == null) {
            send(session, Map.of("type","error","msg","room_not_found"));
            return;
        }
        r.handleAction(p, msg);
    }

    public void fillWithBots(WebSocketSession session) {
        // for testing: find player's room and add bots until 4 players
        Player p = sessionToPlayer.get(session.getId());
        if (p==null) return;
        for (Room r: rooms.values()) {
            if (r.getPlayers().stream().anyMatch(pp->pp.getId().equals(p.getId()))) {
                while (r.getPlayers().size() < 4) r.addPlayer(Player.bot("Bot"+new Random().nextInt(99)));
                return;
            }
        }
    }

    public void leave(WebSocketSession session) {
        Player p = sessionToPlayer.remove(session.getId());
        if (p == null) return;
        for (Room r: rooms.values()) r.removePlayer(p.getId());
    }

    public void onDisconnect(WebSocketSession session) {
        leave(session);
    }

    private void tryMatchQueue() {
        scheduler.execute(() -> {
            while (quickQueue.size() >= 4) {
                List<Player> players = new ArrayList<>();
                for (int i=0;i<4;i++) players.add(quickQueue.poll());
                String id = UUID.randomUUID().toString().substring(0,8);
                Room r = new Room(id);
                rooms.put(id, r);
                for (Player p: players) r.addPlayer(p);
                r.startGame();
            }
        });
    }

    public void send(WebSocketSession s, Object o) {
        try {
            if (s != null && s.isOpen()) s.sendMessage(new org.springframework.web.socket.TextMessage(gson.toJson(o)));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
