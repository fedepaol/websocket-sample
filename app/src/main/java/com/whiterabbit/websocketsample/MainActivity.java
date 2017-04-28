package com.whiterabbit.websocketsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.whiterabbit.websocketsample.websocket.ServerConnection;
import com.whiterabbit.websocketsample.websocket.ServerConnection.ConnectionStatus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ServerConnection.ServerListener {
    private final String SERVER_URL = "ws://10.0.2.2:9000";
    private ServerConnection mServerConnection;

    @Bind(R.id.server_connection_status)
    TextView mConnectionStatus;

    @Bind(R.id.message_from_server)
    TextView mMessageFromServer;

    @Bind(R.id.send_message_button)
    Button mSendMessageButton;

    int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mServerConnection = new ServerConnection(SERVER_URL);
        mSendMessageButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServerConnection.connect(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServerConnection.disconnect();
    }

    @OnClick(R.id.send_message_button)
    public void onSendClicked() {
        mServerConnection.sendMessage(String.valueOf(mCounter++));
    }

    @Override
    public void onNewMessage(String message) {
        mMessageFromServer.setText(message);
    }

    @Override
    public void onStatusChange(ConnectionStatus status) {

        String statusMsg = (status == ConnectionStatus.CONNECTED ?
                                    getString(R.string.connected) : getString(R.string.disconnected));
        mConnectionStatus.setText(statusMsg);
        mSendMessageButton.setEnabled(status == ConnectionStatus.CONNECTED);
    }
}
