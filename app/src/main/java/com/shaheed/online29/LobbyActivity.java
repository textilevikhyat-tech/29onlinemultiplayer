package com.shaheed.online29;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.shaheed.online29.ws.WebSocketManager;

import java.util.Map;

public class LobbyActivity extends Activity {
    Button btnCreate, btnQuickMatch, btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        btnCreate = findViewById(R.id.btnCreate);
        btnQuickMatch = findViewById(R.id.btnQuickMatch);
        btnJoin = findViewById(R.id.btnJoin);

        WebSocketManager.get().setCallback(new WebSocketManager.Callback() {
            @Override public void onOpen() {}
            @Override public void onMessage(Map<String, Object> msg) {
                // server responses (e.g., room created, quick match assigned)
                String type = (String) msg.get("type");
                if("roomCreated".equals(type)) {
                    String roomId = (String) msg.get("roomId");
                    Constants.gameId = roomId;
                    startActivity(new Intent(LobbyActivity.this, GameActivity.class));
                } else if("matched".equals(type)) {
                    String roomId = (String) msg.get("roomId");
                    Constants.gameId = roomId;
                    startActivity(new Intent(LobbyActivity.this, GameActivity.class));
                }
            }
            @Override public void onClose() {}
            @Override public void onError(Throwable t) {}
        });
        WebSocketManager.get().connect();

        btnQuickMatch.setOnClickListener(v -> {
            WebSocketManager.get().send(Map.of("type","quickMatch","playerName",Constants.username));
        });

        btnCreate.setOnClickListener(v -> {
            WebSocketManager.get().send(Map.of("type","createRoom","playerName",Constants.username));
        });

        btnJoin.setOnClickListener(v -> {
            startActivity(new Intent(this, JoinRoomActivity.class));
        });
    }
}
