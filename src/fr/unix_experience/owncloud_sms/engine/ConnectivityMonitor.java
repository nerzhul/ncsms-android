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
