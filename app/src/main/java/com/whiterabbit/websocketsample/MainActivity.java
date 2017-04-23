package com.whiterabbit.websocketsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.whiterabbit.websocketsample.websocket.ServerConnection;


import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final String SERVER_URL = "";
    private ServerConnection mServerConnection;

    @Bind(R.id.server_connection_status)
    TextView mConnectionStatus;

    @Bind(R.id.message_from_server)
    TextView mMessageFromServer;

    @Bind(R.id.send_message_button)
    Button mSendMessageButton;

    int mCounter = 0;

    CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mServerConnection = new ServerConnection(SERVER_URL);
        mSendMessageButton.setEnabled(false);
        mDisposable = new CompositeDisposable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Disposable d = mServerConnection
                .statusObservable()
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnectionStateChanged);

        Disposable d1 = mServerConnection
                            .messagesObservable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(message -> mMessageFromServer.setText(message));

        Disposable d2 = mServerConnection
                        .statusObservable()
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(connected -> !connected)
                        .delay(5, TimeUnit.SECONDS)
                        .subscribe(s -> mServerConnection.connect());

        mDisposable.add(d);
        mDisposable.add(d1);
        mDisposable.add(d2);

        mServerConnection.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServerConnection.disconnect();
        mDisposable.clear();
    }

    @OnClick(R.id.send_message_button)
    public void onSendClicked() {
        mServerConnection.sendMessage(String.valueOf(mCounter++));
    }

    private void onConnectionStateChanged(boolean connected) {
        String status = connected ? getString(R.string.connected) : getString(R.string.disconnected);
        mConnectionStatus.setText(status);
        mSendMessageButton.setEnabled(connected);
    }
}
