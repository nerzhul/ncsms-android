package fr.unix_experience.owncloud_sms.notifications;

/*
 *  Copyright (c) 2014-2017, Loic Blot <loic.blot@unix-experience.fr>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationChannel;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;

/**
 * Helper class for showing and canceling ui
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class OCSMSNotificationUI {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "OCSMS_NOTIFICATION";

    public static void notify(Context context, String titleString,
                              String contentString, OCSMSNotificationType type) {
        notify(context, titleString, contentString,
                type.getChannel().getChannelId(), type.getNotificationId());
    }

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     * @see #cancel(Context, OCSMSNotificationType)
     */
    public static void notify(Context context, String titleString, String contentString,
                              String channelId, int notificationId) {
        Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
//        Bitmap picture = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);

//        String ticker = (titleString.length() > 20) ? titleString.substring(0, 20) : titleString;
        String title = res.getString(R.string.ui_notification_title_template, titleString);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(contentString)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Set ticker text (preview) information for this notification.
                //.setTicker(ticker)

                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentString)
                        .setBigContentTitle(title)
                        .setSummaryText(titleString))
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.oc_primary));

        notify(context, builder.build(), notificationId);
    }

    private static void notify(Context context, Notification notification, int notificationId) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels(context, nm);
        nm.notify(OCSMSNotificationUI.NOTIFICATION_TAG, notificationId, notification);
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, String, OCSMSNotificationType)}.
     */
    public static void cancel(Context context, OCSMSNotificationType type) {
        cancel(context, type.getNotificationId());
    }

    public static void cancel(Context context, int notificationId) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(OCSMSNotificationUI.NOTIFICATION_TAG, notificationId);
    }

    private static void createNotificationChannels(Context context, NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (OCSMSNotificationChannel ocsmsChannel : OCSMSNotificationChannel.values()) {
                NotificationChannel channel = new NotificationChannel(
                        ocsmsChannel.getChannelId(),
                        context.getString(ocsmsChannel.getNameResId()),
                        ocsmsChannel.getImportance());

                if (ocsmsChannel.getDescResId() != null) {
                    channel.setDescription(context.getString(ocsmsChannel.getDescResId()));
                }

                nm.createNotificationChannel(channel);
            }
        }
    }

}
