package com.keylesspalace.tusky.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MqttNotification {

    private static final String TAG = MqttNotification.class.getSimpleName();
    private static final String SP_KEY_SALT = "SP_KEY_SALT";
    private final Context context;

    public MqttNotification(@NonNull final Context context) {

        this.context = context;

        Log.d(TAG, "Instance : " + getInstanceId() + ", password : " + getPassword());

        // Initialize push notification client
        // TODO : update broker url
        new MQTTClient(context, "tcp://192.168.0.20:1883", getInstanceId(), getPassword(), getInstanceId(), new NotificationActions() {
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

    private String getInstanceId() {
        // an identifier that is : something unique, not linked to hardware and that we can find again without storing it
        return Settings.Secure.ANDROID_ID;
    }

    /**
     * Not a password user have to remind, only
     * hash(getInstanceId() + getSalt())
     */
    private String getPassword() {
        return sha256(getInstanceId() + getSalt());
    }

    /**
     * Generate salt if required and store it if required
     */
    private String getSalt() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String salt;
        if (!defaultSharedPreferences.contains(SP_KEY_SALT)) {
            salt = UUID.randomUUID().toString();
            SharedPreferences.Editor edit = defaultSharedPreferences.edit();
            edit.putString(SP_KEY_SALT, salt);
            edit.apply();
        } else {
            salt = defaultSharedPreferences.getString(SP_KEY_SALT, "");
        }
        return salt;
    }

    private static String sha256(@NonNull final String s) {
        final String sha256 = "SHA-256";
        try {
            MessageDigest digest = MessageDigest.getInstance(sha256);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String hash = Integer.toHexString(0xFF & aMessageDigest);
                while (hash.length() < 2) {
                    hash = "0" + hash;
                }
                hexString.append(hash);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            Log.e(TAG, "Cannot find hash algorithm : " + noSuchAlgorithmException);
        }
        return null;
    }
}
