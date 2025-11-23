package com.shaheed.online29.ws;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {
    private static WebSocketManager instance;
    private WebSocket ws;
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final String url = "wss://two9onlinemultiplayer.onrender.com/ws";

    public interface Callback {
        void onMessage(Map<String,Object> msg);
        void onOpen();
        void onClose();
    }

    private Callback callback;

    private WebSocketManager() {
        client = new OkHttpClient();
    }

    public static WebSocketManager get() {
        if (instance == null) instance = new WebSocketManager();
        return instance;
    }

    public void setCallback(Callback cb){
        this.callback = cb;
    }

    public void connect() {
        Request request = new Request.Builder().url(url).build();
        ws = client.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket webSocket, Response response) {
                Log.i("WS", "open");
                if(callback!=null) callback.onOpen();
            }
            @Override public void onMessage(WebSocket webSocket, String text) {
                Log.i("WS", "msg: "+text);
                if(callback!=null) callback.onMessage(gson.fromJson(text, Map.class));
            }
            @Override public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000,null);
                if(callback!=null) callback.onClose();
            }
            @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WS","error",t);
                if(callback!=null) callback.onClose();
            }
        });
    }

    public void send(Map<String,Object> m){
        if(ws!=null) ws.send(gson.toJson(m));
    }

    public void close(){
        if(ws!=null) ws.close(1000,null);
    }
}
