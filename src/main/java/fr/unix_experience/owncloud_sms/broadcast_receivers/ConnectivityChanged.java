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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSSync;
import fr.unix_experience.owncloud_sms.engine.AndroidSmsFetcher;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.enums.PermissionID;
import fr.unix_experience.owncloud_sms.jni.SmsBuffer;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;
import fr.unix_experience.owncloud_sms.prefs.PermissionChecker;

public class ConnectivityChanged extends BroadcastReceiver implements ASyncSMSSync {

	@Override
	public void onReceive(Context context, Intent intent) {
		// No account: abort
		Account[] myAccountList = AccountManager.get(context).
				getAccountsByType(context.getString(R.string.account_type));
		if (myAccountList.length == 0) {
			return;
		}

		ConnectivityMonitor cMon = new ConnectivityMonitor(context);

		OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(context);

		if (!prefs.pushOnReceive()) {
			Log.i(ConnectivityChanged.TAG,"ConnectivityChanges.onReceive: pushOnReceive is disabled");
			return;
		}

		// If data is available and previous dataConnectionState was false, then we need to sync
		if (cMon.isValid() && !ConnectivityChanged.dataConnectionAvailable) {
            ConnectivityChanged.dataConnectionAvailable = true;
			Log.i(ConnectivityChanged.TAG,"ConnectivityChanged.onReceive, data conn available");

            if (!PermissionChecker.checkPermission(context, Manifest.permission.READ_SMS,
                    PermissionID.REQUEST_SMS)) {
                return;
            }

			checkMessagesAndSend(context);
		}
		// No data available and previous dataConnectionState was true
		else if (ConnectivityChanged.dataConnectionAvailable && !cMon.isValid()) {
            ConnectivityChanged.dataConnectionAvailable = false;
			Log.i(ConnectivityChanged.TAG,"ConnectivityChanges.onReceive: data conn is off");
		}
	}

	private void checkMessagesAndSend(Context context) {
		// Get last message synced from preferences
		Long lastMessageSynced = (new OCSMSSharedPrefs(context)).getLastMessageDate();
		Log.i(ConnectivityChanged.TAG,"Synced Last:" + lastMessageSynced);

		// Now fetch messages since last stored date
		SmsBuffer smsBuffer = new SmsBuffer();
		new AndroidSmsFetcher(context).bufferMessagesSinceDate(smsBuffer, lastMessageSynced);

		AtomicReference<ConnectivityMonitor> cMon = new AtomicReference<>(new ConnectivityMonitor(context));

		// Synchronize if network is valid and there are SMS
		if (cMon.get().isValid() && !smsBuffer.empty()) {
			new SyncTask(context, smsBuffer).execute();
		}
	}

	private static boolean dataConnectionAvailable = false;

	private static final String TAG = ConnectivityChanged.class.getSimpleName();
}
