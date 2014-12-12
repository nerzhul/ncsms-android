package fr.unix_experience.owncloud_sms.broadcast_receivers;

import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityChanged extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"ConnectivityChanged.onReceive");
		(new OCSMSNotificationManager(context)).setDebugMsg("ConnectivityChanged");
	}
	
	private static final String TAG = ConnectivityChanged.class.getSimpleName();
}
