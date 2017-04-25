/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MQTTClient {

    private static final String TAG = MQTTClient.class.getSimpleName();
    private static final String CLIENT_NAME = "TuskyMastodonClient";
    private static final String SUBSCRIPTION_TOPIC_BASE = "tusky/notification/";

    private MqttAndroidClient mqttAndroidClient;
    private String serverUri;
    private NotificationActions notificationActions;
    private ArrayList<String> subscribedTopics;

    public MQTTClient(@NonNull Context context, @NonNull String serverUri, @NonNull final NotificationActions notificationActions) {
        this.serverUri = serverUri;
        this.notificationActions = notificationActions;
        this.subscribedTopics = new ArrayList<>();

        String clientId = String.format(Locale.getDefault(), "%s/%s/%s", CLIENT_NAME, System.currentTimeMillis(), UUID.randomUUID().toString());
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.v(TAG, "MQTT Connection complete");
                if (reconnect) {
                    // Recover subscribed topics
                    for (String topic : subscribedTopics) {
                        subscribeToTopic(topic);
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.v(TAG, "MQTT Connection lost");
                MQTTClient.this.notificationActions.onConnectionLost(cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String strMessage = new String(message.getPayload());
                Log.v(TAG, "Incoming message: " + message);
                MQTTClient.this.notificationActions.onMessageReceived(strMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Ignored
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    notificationActions.onConnectionComplete();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v(TAG, "Failed to connect to MQTT broker");
                    MQTTClient.this.notificationActions.onConnectionFailed(exception);
                }
            });
        } catch (MqttException ex) {
            Log.e(TAG, "Exception whilst connecting");
            notificationActions.onConnectionFailed(ex);
        }
    }

    public void subscribeToTopic(@NonNull String topic) {
        final String fullTopicName = String.format(Locale.getDefault(), "%s/%s", SUBSCRIPTION_TOPIC_BASE, topic);
        subscribedTopics.add(topic);
        try {
            mqttAndroidClient.subscribe(fullTopicName, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.v(TAG, "Subscribed to " + fullTopicName + " on " + serverUri);
                    notificationActions.onConnectionComplete();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.v(TAG, "Failed to subscribe");
                    notificationActions.onConnectionFailed(exception);
                }
            });
        } catch (MqttException ex) {
            Log.e(TAG, "Exception whilst subscribing");
            notificationActions.onConnectionFailed(ex);
        }
    }

    public void unsubscribeToTopic(@NonNull String topic) {
        subscribedTopics.remove(topic);

        try {
            mqttAndroidClient.unsubscribe(getFullTopicName(topic));
        } catch (MqttException ex) {
            Log.e(TAG, "Exception whilst unsubscribing");
            notificationActions.onConnectionFailed(ex);
        }
    }

    private String getFullTopicName(String topic) {
        if (topic.contains("/")) {
            throw new IllegalArgumentException("Invalid topic name");
        }
        return String.format(Locale.getDefault(), "%s/%s", SUBSCRIPTION_TOPIC_BASE, topic);
    }
}
