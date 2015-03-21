package fr.unix_experience.owncloud_sms.engine;

/*
 *  Copyright (c) 2014-2015, Loic Blot <loic.blot@unix-experience.fr>
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
 */ 

import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

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
			// Load the connectivity manager to determine on which network we are connected
			NetworkInfo netInfo = _cMgr.getActiveNetworkInfo();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
			
			// Check
			switch (netInfo.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				return prefs.getBoolean("sync_wifi", DefaultPrefs.syncWifi);
			case ConnectivityManager.TYPE_MOBILE:
				switch (netInfo.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					return prefs.getBoolean("sync_2g", DefaultPrefs.sync2G);
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return prefs.getBoolean("sync_gprs", DefaultPrefs.syncGPRS);
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_UMTS:	
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					return prefs.getBoolean("sync_3g", DefaultPrefs.sync3G);
				case TelephonyManager.NETWORK_TYPE_LTE:
					return prefs.getBoolean("sync_4g", DefaultPrefs.sync3G);
				default:
					return prefs.getBoolean("sync_others", DefaultPrefs.syncOthers);
				}
			default:
				return prefs.getBoolean("sync_others", DefaultPrefs.syncOthers);
			}
			
			
		}
		
		return false;
	}
	
	private ConnectivityManager _cMgr;
	private Context _context;
}
