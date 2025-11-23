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
        textViewusername.setText(Constants.username + " | Game: " + Constants.gameId);

        // 1) Connect WebSocket
        WSClient.getInstance().connect();

        // 2) Send joinRoom request
        try {
            JSONObject join = new JSONObject();
            join.put("type", "joinRoom");
            join.put("roomId", Constants.gameId);
            join.put("playerName", Constants.username);
            WSClient.getInstance().send(join);

        } catch (Exception e) {
            Log.e("WS", e.toString());
        }
    }
}
