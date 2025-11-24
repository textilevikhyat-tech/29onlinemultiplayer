package com.shaheed.online29;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.TranslateAnimation;

import com.shaheed.online29.game.GameEngine;
import com.shaheed.online29.ws.WebSocketManager;

import java.util.List;
import java.util.Map;

public class GameActivity extends Activity {

    TextView tvStatus;
    FrameLayout table;
    GameEngine engine;
    List<String> myHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_table);

        tvStatus = findViewById(R.id.tvStatus);
        table = findViewById(R.id.tableLayout);

        engine = new GameEngine();
        engine.shuffle();
        myHand = engine.dealHand(8);

        showHand(myHand);

        WebSocketManager.get().setCallback(new WebSocketManager.Callback() {
            @Override public void onOpen() {}
            @Override public void onMessage(Map<String, Object> msg) {
                runOnUiThread(() -> handleServerMessage(msg));
            }
            @Override public void onClose() {}
            @Override public void onError(Throwable t) {}
        });
        // ensure connection (if not connected yet)
        WebSocketManager.get().connect();
    }

    private void showHand(List<String> hand){
        table.removeAllViews();
        int x = 50;
        for(String card : hand){
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.card_back);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(200,300);
            lp.leftMargin = x;
            lp.topMargin = 700;
            table.addView(iv, lp);
            x += 120;
            TranslateAnimation anim = new TranslateAnimation(0,0, -500,0);
            anim.setDuration(400);
            iv.startAnimation(anim);
        }
    }

    private void handleServerMessage(Map<String,Object> msg){
        String type = (String) msg.get("type");
        Log.i("GAME","msg: "+type);
        if("deal".equals(type)){
            tvStatus.setText("Dealt");
        } else if("play".equals(type)){
            tvStatus.setText("Play: "+msg.get("card"));
        } else if("start".equals(type)){
            tvStatus.setText("Game started");
        }
    }
}
