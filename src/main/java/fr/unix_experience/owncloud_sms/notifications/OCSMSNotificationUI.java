package fr.unix_experience.owncloud_sms.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import fr.unix_experience.owncloud_sms.R;

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

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     * @see #cancel(Context)
     */
    public static void notify(Context context, String titleString,
                              String contentString, int number) {
        Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        Bitmap picture = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);


        String ticker = (titleString.length() > 20) ? titleString.substring(0, 20) : titleString;
        String title = res.getString(R.string.ui_notification_title_template, titleString);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(contentString)

                        // All fields below this line are optional.

                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        // Provide a large icon, shown with the notification in the
                        // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                        // Set ticker text (preview) information for this notification.
                //.setTicker(ticker)

                        // Show a number. This is useful when stacking notifications of
                        // a single type.
                .setNumber(number)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentString)
                        .setBigContentTitle(title)
                        .setSummaryText(titleString))
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.oc_primary));

        OCSMSNotificationUI.notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(Context context, Notification notification) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(OCSMSNotificationUI.NOTIFICATION_TAG, 0, notification);
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(Context context) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(OCSMSNotificationUI.NOTIFICATION_TAG, 0);
    }
}
