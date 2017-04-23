package com.whiterabbit.websocketsample.websocket;


import com.jakewharton.rxrelay2.PublishRelay;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;



public class ServerConnection extends WebSocketListener {
    private WebSocket webSocket;
    private OkHttpClient client;
    private String serverUrl;
    private PublishRelay<String> messageRelay;
    private PublishRelay<Boolean> statusRelay;

    public ServerConnection(String url) {
        client = new OkHttpClient.Builder()
                .readTimeout(3,  TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        serverUrl = url;

        messageRelay = PublishRelay.create();
        statusRelay = PublishRelay.create();
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(serverUrl)
                .build();
        webSocket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        webSocket.cancel();
        statusRelay.accept(false);
    }

    public void sendMessage(String message) {
        webSocket.send(message);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        messageRelay.accept(text);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        statusRelay.accept(true);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        statusRelay.accept(false);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        disconnect();
    }

    public Observable<String> messagesObservable() {
        return messageRelay;
    }

    public Observable<Boolean> statusObservable() {
        return statusRelay;
    }
}
