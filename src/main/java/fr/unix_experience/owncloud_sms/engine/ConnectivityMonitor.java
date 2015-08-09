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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class ConnectivityMonitor {
	public ConnectivityMonitor(final Context context) {
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
			final NetworkInfo netInfo = _cMgr.getActiveNetworkInfo();
			if (netInfo == null) {
				return false;
			}

			final OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(_context);

			// Check
			switch (netInfo.getType()) {
			case ConnectivityManager.TYPE_WIFI:
				return prefs.syncInWifi();
			case ConnectivityManager.TYPE_MOBILE:
				switch (netInfo.getSubtype()) {
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					return prefs.syncIn2G();
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return prefs.syncInGPRS();
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					return prefs.syncIn3G();
				case TelephonyManager.NETWORK_TYPE_LTE:
					return prefs.syncIn4G();
				default:
					return prefs.syncInOtherModes();
				}
			default:
				return prefs.syncInOtherModes();
			}
		}

		return false;
	}

	private ConnectivityManager _cMgr;
	private final Context _context;
}
