package com.shaheed.online29;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.shaheed.online29.ws.WebSocketManager;

import java.util.Map;

public class JoinRoomActivity extends Activity {
    EditText etRoomId;
    Button btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        etRoomId = findViewById(R.id.etRoomId);
        btnJoin = findViewById(R.id.btnJoinRoom);

        btnJoin.setOnClickListener(v -> {
            String room = etRoomId.getText().toString().trim();
            if(!room.isEmpty()){
                WebSocketManager.get().send(Map.of("type","joinRoom","roomId",room,"playerName",Constants.username));
            }
        });
    }
}
