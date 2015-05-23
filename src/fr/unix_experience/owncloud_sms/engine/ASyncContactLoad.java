package fr.unix_experience.owncloud_sms.engine;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;

public interface ASyncContactLoad {
	class ContactLoadTask extends AsyncTask<Void, Void, Void> {
		private static AccountManager _accountMgr = null;
		private static Account _account;
		private final Context _context;

		public ContactLoadTask(final Account account, final Context context) {
			if (_accountMgr == null) {
				_accountMgr = AccountManager.get(context);
			}

			_account = account;
			_context = context;
		}
		@Override
		protected Void doInBackground(final Void... params) {
			// Create client
			final String ocURI = _accountMgr.getUserData(_account, "ocURI");
			if (ocURI == null) {
				// @TODO: Handle the problem
				return null;
			}

			final Uri serverURI = Uri.parse(ocURI);

			final OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(_context,
					serverURI, _accountMgr.getUserData(_account, "ocLogin"),
					_accountMgr.getPassword(_account));

			try {
				if (_client.getServerAPIVersion() < 2) {
					// @TODO: handle error
				}
				_client.getServerPhoneNumbers();
			} catch (final OCSyncException e) {
				// @TODO: handle error
			}
			return null;

		}
	}

	static final String TAG = ASyncSMSSync.class.getSimpleName();
}
