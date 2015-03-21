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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.json.JSONArray;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationManager;

public interface ASyncTask {
	class SyncTask extends AsyncTask<Void, Void, Void>{
		public SyncTask(final Context context, final JSONArray smsList) {
			_context = context;
			_smsList = smsList;
		}

		@Override
		protected Void doInBackground(final Void... params) {
			final OCSMSNotificationManager nMgr = new OCSMSNotificationManager(_context);

			// Get ownCloud SMS account list
			final AccountManager _accountMgr = AccountManager.get(_context);

			final Account[] myAccountList = _accountMgr.getAccountsByType(_context.getString(R.string.account_type));
			for (final Account element : myAccountList) {
				final Uri serverURI = Uri.parse(_accountMgr.getUserData(element, "ocURI"));

				final OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(_context,
						serverURI, _accountMgr.getUserData(element, "ocLogin"),
						_accountMgr.getPassword(element));

				try {
					_client.doPushRequest(_smsList);
				} catch (final OCSyncException e) {
					Log.e(TAG, _context.getString(e.getErrorId()));
				}
			}
			nMgr.dropSyncProcessMsg();
			return null;
		}

		private final Context _context;
		private final JSONArray _smsList;
	}

	static final String TAG = ASyncTask.class.getSimpleName();
}
