package com.mygame.server;

public class GameRoom {
    private String roomId;

    public GameRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
