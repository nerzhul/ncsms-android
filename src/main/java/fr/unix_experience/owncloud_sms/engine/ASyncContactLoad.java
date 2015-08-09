package fr.unix_experience.owncloud_sms.engine;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.adapters.ContactListAdapter;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;

public interface ASyncContactLoad {
	class ContactLoadTask extends AsyncTask<Void, Void, Boolean> {
		private static AccountManager _accountMgr = null;
		private static Account _account;
		private final Context _context;
		private ContactListAdapter _adapter;
		private ArrayList<String> _objects;

		public ContactLoadTask(final Account account, final Context context,
				ContactListAdapter adapter, ArrayList<String> objects) {
			if (_accountMgr == null) {
				_accountMgr = AccountManager.get(context);
			}

			_account = account;
			_context = context;
			_adapter = adapter;
			_objects = objects;
		}
		@Override
		protected Boolean doInBackground(final Void... params) {
			// Create client
			final String ocURI = _accountMgr.getUserData(_account, "ocURI");
			if (ocURI == null) {
				// @TODO: Handle the problem
				return false;
			}

			final Uri serverURI = Uri.parse(ocURI);

			final OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(_context,
					serverURI, _accountMgr.getUserData(_account, "ocLogin"),
					_accountMgr.getPassword(_account));

			// Remove all objects, due to refreshing handling
			_objects.clear();
			try {
				if (_client.getServerAPIVersion() < 2) {
					_objects.add(_context.getString(R.string.err_proto_v2));
					return false;
				}

				JSONArray phoneNumbers = _client.getServerPhoneNumbers();
				for (int i = 0; i < phoneNumbers.length(); i++) {
					String phone = phoneNumbers.getString(i);
					_objects.add(phone);
				}

				// Sort phone numbers
				Collections.sort(_objects);
			} catch (JSONException e) {
				_objects.add(_context.getString(R.string.err_fetch_phonelist));
				return false;
			} catch (final OCSyncException e) {
				_objects.add(_context.getString(e.getErrorId()));
				return false;
			}
			return true;

		}

		protected void onPostExecute(final Boolean success) {
			_adapter.notifyDataSetChanged();
		}
	}

	static final String TAG = ASyncContactLoad.class.getSimpleName();
}
