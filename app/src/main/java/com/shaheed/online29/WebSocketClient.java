package com.shaheed.online29;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.google.gson.Gson;

public class WebSocketClient29 {

    private static WebSocketClient29 instance;
    private WebSocketClient client;
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessage(Map<String, Object> message);
    }

    private MessageListener listener;

    public static WebSocketClient29 getInstance() {
        if (instance == null) instance = new WebSocketClient29();
        return instance;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void connect() {
        try {
            URI uri = new URI("wss://two9onlinemultiplayer.onrender.com/ws/game");

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d("WS29", "Connected to WebSocket");
                }

                @Override
                public void onMessage(String message) {
                    Log.d("WS29", "Message: " + message);
                    Map<String, Object> map = gson.fromJson(message, Map.class);

                    if (listener != null) listener.onMessage(map);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WS29", "Disconnected: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e("WS29", "Error", ex);
                }
            };

            client.connect();

        } catch (URISyntaxException e) {
            Log.e("WS29", "Bad URI", e);
        }
    }

    public void send(Map<String, Object> data) {
        if (client != null && client.isOpen()) {
            client.send(gson.toJson(data));
        }
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }

    public void disconnect() {
        if (client != null) client.close();
    }
}
