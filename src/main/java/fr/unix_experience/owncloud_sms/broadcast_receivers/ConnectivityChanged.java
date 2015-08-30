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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSSync;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class ConnectivityChanged extends BroadcastReceiver implements ASyncSMSSync {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// No account: abort
		final Account[] myAccountList = AccountManager.get(context).
				getAccountsByType(context.getString(R.string.account_type));
		if (myAccountList.length == 0) {
			return;
		}

		final ConnectivityMonitor cMon = new ConnectivityMonitor(context);

		final OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(context);

		if (!prefs.pushOnReceive()) {
			Log.d(TAG,"ConnectivityChanges.onReceive: pushOnReceive is disabled");
			return;
		}

		// If data is available and previous dataConnectionState was false, then we need to sync
		if (cMon.isValid() && dataConnectionAvailable == false) {
			dataConnectionAvailable = true;
			Log.d(TAG,"ConnectivityChanged.onReceive, data conn available");
			checkMessagesAndSend(context);
		}
		// No data available and previous dataConnectionState was true
		else if (dataConnectionAvailable == true && !cMon.isValid()) {
			dataConnectionAvailable = false;
			Log.d(TAG,"ConnectivityChanges.onReceive: data conn is off");
		}
	}

	private void checkMessagesAndSend(final Context context) {

		// Get last message synced from preferences
		final Long lastMessageSynced = (new OCSMSSharedPrefs(context)).getLastMessageDate();
		Log.d(TAG,"Synced Last:" + lastMessageSynced);

		// Now fetch messages since last stored date
		final JSONArray smsList = new SmsFetcher(context).bufferizeMessagesSinceDate(lastMessageSynced);

		final ConnectivityMonitor cMon = new ConnectivityMonitor(context);

		// Synchronize if network is valid and there are SMS
		if (cMon.isValid() && smsList != null) {
			new SyncTask(context, smsList).execute();
		}
	}

	private static boolean dataConnectionAvailable = false;

	private static final String TAG = ConnectivityChanged.class.getSimpleName();
}
