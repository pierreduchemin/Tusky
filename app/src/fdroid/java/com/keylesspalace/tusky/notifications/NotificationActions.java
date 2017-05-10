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

public interface NotificationActions {

    /**
     * A push message have been received
     *
     * @param topic   the MQTT topic where message have been posted.
     *                In the context of Tusky, in order to avoid conflicts, it can't contain "/".
     * @param message the message content
     */
    void onMessageReceived(String topic, String message);

    /**
     * MQTT connection is complete, we are now able to subscribe/unsubscribe to additional topics
     * and disconnect
     */
    void onConnectionComplete();

    /**
     * MQTT connection was active, but have been lost
     */
    void onConnectionLost(Throwable exception);

    /**
     * Not able to connect to MQTT broker
     */
    void onConnectionFailed(Throwable exception);

    /**
     * Not able to disconnect from MQTT broker
     */
    void onDisconnectFailed(Throwable exception);
}
