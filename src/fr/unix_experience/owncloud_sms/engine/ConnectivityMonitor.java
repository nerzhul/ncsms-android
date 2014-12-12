package fr.unix_experience.owncloud_sms.engine;

import android.content.Context;
import android.net.ConnectivityManager;

public class ConnectivityMonitor {
	public ConnectivityMonitor(Context context) {
		_context = context;
	}
	
	// Valid connection = WiFi or Mobile data
	public boolean isValid() {
		if (_cMgr == null) {
			_cMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		
		final android.net.NetworkInfo niWiFi = _cMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo niMobile = _cMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		if (niWiFi.isAvailable() || niMobile.isAvailable()) {
			return true;
		}
		
		return false;
	}
	
	private ConnectivityManager _cMgr;
	private Context _context;
}
