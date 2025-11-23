package com.shaheed.online29;

import android.util.Log;

import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MyWebSocketClient {

    private WebSocketClient webSocketClient;
    private static MyWebSocketClient instance;
    private static final String WS_URL = "wss://two9onlinemultiplayer.onrender.com/ws";

    public static MyWebSocketClient getInstance() {
        if (instance == null) instance = new MyWebSocketClient();
        return instance;
    }

    public void connect() {
        try {
            URI uri = new URI(WS_URL);

            webSocketClient = new WebSocketClient(uri) {

                @Override
                public void onOpen() {
                    Log.d("WS", "Connected to server");
                }

                @Override
                public void onTextReceived(String message) {
                    Log.d("WS", "Received: " + message);
                    // yaha tum game events handle kar sakte ho
                }

                @Override
                public void onBinaryReceived(byte[] data) {}

                @Override
                public void onPingReceived(byte[] data) {}

                @Override
                public void onPongReceived(byte[] data) {}

                @Override
                public void onException(Exception e) {
                    Log.e("WS", "Error: " + e.getMessage());
                }

                @Override
                public void onCloseReceived() {
                    Log.d("WS", "Disconnected");
                }
            };

            webSocketClient.setConnectTimeout(10000);
            webSocketClient.setReadTimeout(60000);
            webSocketClient.enableAutomaticReconnection(5000);

            webSocketClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(JSONObject json) {
        if (webSocketClient != null) {
            webSocketClient.send(json.toString());
        }
    }
}
