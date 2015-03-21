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
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.OCSMSOwnCloudClient;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.notifications.OCSMSNotificationManager;

public class SmsSyncAdapter extends AbstractThreadedSyncAdapter {

	public SmsSyncAdapter(final Context context, final boolean autoInitialize) {
		super(context, autoInitialize);
		_accountMgr = AccountManager.get(context);
	}

	@Override
	public void onPerformSync(final Account account, final Bundle extras, final String authority,
			final ContentProviderClient provider, final SyncResult syncResult) {

		final OCSMSNotificationManager nMgr = new OCSMSNotificationManager(getContext());

		// Create client
		final String ocURI = _accountMgr.getUserData(account, "ocURI");
		if (ocURI == null) {
			nMgr.setSyncErrorMsg(getContext().getString(R.string.err_sync_account_unparsable));
			return;
		}

		final Uri serverURI = Uri.parse(ocURI);
		nMgr.setSyncProcessMsg();

		final OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(getContext(),
				serverURI, _accountMgr.getUserData(account, "ocLogin"),
				_accountMgr.getPassword(account));

		try {
			// getServerAPI version
			Log.d(TAG,"Server API version: " + _client.getServerAPIVersion());

			// and push datas
			_client.doPushRequest(null);

			nMgr.dropSyncErrorMsg();
		} catch (final OCSyncException e) {
			nMgr.setSyncErrorMsg(getContext().getString(e.getErrorId()));
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
				// UNHANDLED
			}
		}

		nMgr.dropSyncProcessMsg();

	}

	private final AccountManager _accountMgr;

	private static final String TAG = SmsSyncAdapter.class.getSimpleName();
}
