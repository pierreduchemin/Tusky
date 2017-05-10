package com.keylesspalace.tusky.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class MqttNotification {

    public MqttNotification(@NonNull final Context context) {

        // Initialize push notification client
        // TODO : update broker url
        new MQTTClient(context, "tcp://10.0.0.95:1883", getUuid(), getPassword(), getUuid(), new NotificationActions() {
            @Override
            public void onMessageReceived(String topic, String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionComplete() {

            }

            @Override
            public void onConnectionLost(Throwable exception) {

            }

            @Override
            public void onConnectionFailed(Throwable exception) {

            }

            @Override
            public void onDisconnectFailed(Throwable exception) {

            }
        });
    }

    private String getUuid() {
        return "";
    }

    private String getPassword() {
        // TODO : return hash(getUuid() + getSalt())
        return "";
    }

    private String getSalt() {
        // TODO : generate salt if required and store it
        return "";
    }
}
