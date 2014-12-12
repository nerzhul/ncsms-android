package fr.unix_experience.owncloud_sms.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class ConnectivityChanged extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {		
		// Check the connectivity
		final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		final android.net.NetworkInfo niWiFi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo niMobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		// If data is available and previous dataConnectionState was false, then we need to sync
		if ((niWiFi.isAvailable() || niMobile.isAvailable()) && dataConnectionAvailable == false) {
			dataConnectionAvailable = true;
			Log.d(TAG,"ConnectivityChanged.onReceive, data conn available");
			// @TODO: check if last message is last synced msg (shared preference)
		}
		// No data available and previous dataConnectionState was true
		else if (dataConnectionAvailable == true && !niWiFi.isAvailable() && !niMobile.isAvailable()) {
			dataConnectionAvailable = false;
		}
	}
	
	private static boolean dataConnectionAvailable = false;
	
	private static final String TAG = ConnectivityChanged.class.getSimpleName();
}
