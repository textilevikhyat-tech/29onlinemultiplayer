package com.shaheed.online29;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WSClient extends WebSocketClient {

    public interface Listener {
        void onMessageReceived(String msg);
    }

    private static WSClient instance;
    private static Listener listener;

    public static WSClient getInstance() {
        return instance;
    }

    public static void connect(String url, Listener ls) {
        listener = ls;
        try {
            instance = new WSClient(new URI(url));
            instance.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WSClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.d("WS", "Connected to WebSocket");
    }

    @Override
    public void onMessage(String message) {
        if (listener != null) listener.onMessageReceived(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("WS", "WS Closed " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void sendJSON(String json) {
        send(json);
    }
}
