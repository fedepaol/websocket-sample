package com.whiterabbit.websocketsample.websocket;


import com.jakewharton.rxrelay2.PublishRelay;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class ServerConnection {
    private WebSocket mWebSocket;
    private OkHttpClient mClient;
    private String mServerUrl;
    private PublishRelay<String> mMessageRelay;
    private PublishRelay<Boolean> mStatusRelay;

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            mStatusRelay.accept(true);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            mMessageRelay.accept(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            mStatusRelay.accept(false);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            disconnect();
        }
    }

    public ServerConnection(String url) {
        mClient = new OkHttpClient.Builder()
                .readTimeout(3,  TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        mServerUrl = url;

        mMessageRelay = PublishRelay.create();
        mStatusRelay = PublishRelay.create();
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(mServerUrl)
                .build();
        mWebSocket = mClient.newWebSocket(request, new SocketListener());
    }

    public void disconnect() {
        mWebSocket.cancel();
        mStatusRelay.accept(false);
    }

    public void sendMessage(String message) {
        mWebSocket.send(message);
    }

    public Observable<String> messagesObservable() {
        return mMessageRelay;
    }

    public Observable<Boolean> statusObservable() {
        return mStatusRelay;
    }
}
