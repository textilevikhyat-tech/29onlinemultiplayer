package com.shaheed.online29;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

public class GameActivity extends Activity {

    TextView textViewusername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textViewusername = findViewById(R.id.gametextViewusername);

        textViewusername.setText(
                Constants.username + " | GameID: " + Constants.gameId
        );

        // WebSocket connect
        MyWebSocketClient.getInstance().connect();

        // Join game message
        try {
            JSONObject joinMsg = new JSONObject();
            joinMsg.put("type", "joinRoom");
            joinMsg.put("roomId", Constants.gameId);
            joinMsg.put("playerName", Constants.username);
            MyWebSocketClient.getInstance().send(joinMsg);
        } catch (Exception e) {
            Log.e("WS", "JSON error: " + e.getMessage());
        }
    }
}
