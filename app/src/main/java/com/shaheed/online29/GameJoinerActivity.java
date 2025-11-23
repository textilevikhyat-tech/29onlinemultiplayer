package com.shaheed.online29;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class GameJoinerActivity extends Activity {

    TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_joiner);

        textViewStatus = findViewById(R.id.joingameTextviewStatus);
        textViewStatus.setText("Connecting...");

        // WebSocket connect
        WSClient.getInstance().connect();

        // Direct open game screen
        Intent in = new Intent(this, GameActivity.class);
        startActivity(in);
        finish();
    }
}
