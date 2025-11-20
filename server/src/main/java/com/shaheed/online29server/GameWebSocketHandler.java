package com.shaheed.online29server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.lang.reflect.Type;
import java.util.Map;

public class GameWebSocketHandler extends TextWebSocketHandler {
    private static final RoomManager manager = RoomManager.getInstance();
    private static final Gson gson = new Gson();
    private static final Type MAPTYPE = new TypeToken<Map<String,Object>>(){}.getType();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // nothing until client sends "join"/"quickMatch"
        manager.registerSession(session);
        send(session, Map.of("type","connected","msg","welcome"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String,Object> msg = gson.fromJson(payload, MAPTYPE);
        String type = (String) msg.get("type");
        if (type == null) {
            send(session, Map.of("type","error","msg","missing_type"));
            return;
        }

        switch (type) {
            case "quickMatch": {
                String name = (String) msg.getOrDefault("playerName","Guest");
                manager.joinQuickMatch(session, name);
                break;
            }
            case "createRoom": {
                String name = (String) msg.getOrDefault("playerName","Host");
                manager.createRoom(session, name);
                break;
            }
            case "joinRoom": {
                String roomId = (String) msg.get("roomId");
                String name = (String) msg.getOrDefault("playerName","Guest");
                manager.joinRoom(session, roomId, name);
                break;
            }
            case "action": {
                // action forwarded to room/game engine
                manager.handlePlayerAction(session, msg);
                break;
            }
            case "botFill": {
                // helper to request bots immediately (for testing)
                manager.fillWithBots(session);
                break;
            }
            case "leave": {
                manager.leave(session);
                break;
            }
            default:
                send(session, Map.of("type","error","msg","unknown_type"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        manager.onDisconnect(session);
    }

    private void send(WebSocketSession s, Object o) {
        try {
            if (s != null && s.isOpen()) s.sendMessage(new TextMessage(gson.toJson(o)));
        } catch (Exception e){ e.printStackTrace(); }
    }
}
