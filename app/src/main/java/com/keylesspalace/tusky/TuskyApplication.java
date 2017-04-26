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

package com.keylesspalace.tusky;

import android.app.Application;
import android.net.Uri;
import android.widget.Toast;

import com.keylesspalace.tusky.notifications.MQTTClient;
import com.keylesspalace.tusky.notifications.NotificationActions;
import com.squareup.picasso.Picasso;
import com.jakewharton.picasso.OkHttp3Downloader;

public class TuskyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Picasso configuration
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this));
        if (BuildConfig.DEBUG) {
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();
                }
            });
        }

        try {
            Picasso.setSingletonInstance(builder.build());
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }

        if (BuildConfig.DEBUG) {
            Picasso.with(this).setLoggingEnabled(true);
        }

        // Initialize push notification client
        new MQTTClient(getApplicationContext(), "tcp://192.168.52.84:1883", "test", new NotificationActions() {
            @Override
            public void onMessageReceived(String topic, String message) {
                Toast.makeText(TuskyApplication.this, message, Toast.LENGTH_SHORT).show();
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
}