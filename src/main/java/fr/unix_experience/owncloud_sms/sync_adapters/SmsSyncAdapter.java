package fr.unix_experience.owncloud_sms.sync_adapters;

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

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.OCSMSOwnCloudClient;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationUI;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

class SmsSyncAdapter extends AbstractThreadedSyncAdapter {

	SmsSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

		if (new OCSMSSharedPrefs(getContext()).showSyncNotifications()) {
			OCSMSNotificationUI.notify(getContext(), getContext().getString(R.string.sync_title),
					getContext().getString(R.string.sync_inprogress), OCSMSNotificationType.SYNC);
		}

		try {
			OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(getContext(), account);

			// getServerAPI version
			Log.i(SmsSyncAdapter.TAG, "Server API version: " + _client.getServerAPIVersion());

			// and push datas
			_client.doPushRequest(null);
			OCSMSNotificationUI.cancel(getContext(), OCSMSNotificationType.SYNC_FAILED);
		} catch (IllegalStateException e) {
			OCSMSNotificationUI.notify(getContext(), getContext().getString(R.string.fatal_error),
					e.getMessage(), OCSMSNotificationType.SYNC_FAILED);
		} catch (OCSyncException e) {
            OCSMSNotificationUI.notify(getContext(), getContext().getString(R.string.fatal_error),
                    getContext().getString(e.getErrorId()), OCSMSNotificationType.SYNC_FAILED);
			if (e.getErrorType() == OCSyncErrorType.IO) {
				syncResult.stats.numIoExceptions++;
			}
			else if (e.getErrorType() == OCSyncErrorType.PARSE) {
				syncResult.stats.numParseExceptions++;
			}
			else if (e.getErrorType() == OCSyncErrorType.AUTH) {
				syncResult.stats.numAuthExceptions++;
			}
			else {
				Log.w(SmsSyncAdapter.TAG, "onPerformSync: unhandled response");
			}
		} finally {
			OCSMSNotificationUI.cancel(getContext(), OCSMSNotificationType.SYNC);
		}
	}

	private static final String TAG = SmsSyncAdapter.class.getSimpleName();
}
