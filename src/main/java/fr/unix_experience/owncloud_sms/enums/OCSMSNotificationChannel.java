package fr.unix_experience.owncloud_sms.enums;

import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.StringRes;

import fr.unix_experience.owncloud_sms.R;

public enum OCSMSNotificationChannel {
    DEFAULT("OCSMS_DEFAULT", R.string.notification_channel_name_default, null),
    SYNC("OCSMS_SYNC", R.string.notification_channel_name_sync, null);

    static {
        // well, that's a bit of a hack :/
        // can be inlined in the future
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DEFAULT.importance = NotificationManager.IMPORTANCE_DEFAULT;
            SYNC.importance = NotificationManager.IMPORTANCE_LOW;
        }
    }

    private final String channelId;
    private final int nameResId;
    private final Integer descResId;
    private int importance;

    OCSMSNotificationChannel(String channelId, @StringRes int nameResId, @StringRes Integer descResId) {
        this.channelId = channelId;
        this.nameResId = nameResId;
        this.descResId = descResId;
    }

    public String getChannelId() {
        return channelId;
    }

    public int getNameResId() {
        return nameResId;
    }

    public Integer getDescResId() {
        return descResId;
    }

    public int getImportance() {
        return importance;
    }
}
