package fr.unix_experience.owncloud_sms.broadcast_receivers;

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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.json.JSONArray;

import fr.unix_experience.owncloud_sms.engine.ASyncTask;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityChanged extends BroadcastReceiver implements ASyncTask {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityMonitor cMon = new ConnectivityMonitor(context);
		// If data is available and previous dataConnectionState was false, then we need to sync
		if (cMon.isValid() && dataConnectionAvailable == false) {
			dataConnectionAvailable = true;
			Log.d(TAG,"ConnectivityChanged.onReceive, data conn available");
			checkMessagesToSent(context);
		}
		// No data available and previous dataConnectionState was true
		else if (dataConnectionAvailable == true && !cMon.isValid()) {
			dataConnectionAvailable = false;
			Log.d(TAG,"ConnectivityChanges.onReceive: data conn is off");
		}
	}
	
	private void checkMessagesToSent(Context context) {
		// Get last message synced from preferences
		Long lastMessageSynced = (new OCSMSSharedPrefs(context)).getLastMessageDate();
		Log.d(TAG,"Synced Last:" + lastMessageSynced);
		
		// Now fetch messages since last stored date
		JSONArray smsList = new SmsFetcher(context).bufferizeMessagesSinceDate(lastMessageSynced);
		
		if (smsList != null) {
			new SyncTask(context, smsList).execute();
		}
	}
	
	private static boolean dataConnectionAvailable = false;
	
	private static final String TAG = ConnectivityChanged.class.getSimpleName();
}
