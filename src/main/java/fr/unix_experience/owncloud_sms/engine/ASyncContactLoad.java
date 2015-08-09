package fr.unix_experience.owncloud_sms.engine;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
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

			try {
				if (_client.getServerAPIVersion() < 2) {
					// @TODO: handle error
					return false;
				}

				JSONArray phoneNumbers = _client.getServerPhoneNumbers();
				Log.d(TAG, phoneNumbers.toString());
				for (int i = 0; i < phoneNumbers.length(); i++) {
					String phone = phoneNumbers.getString(i);
					_objects.add(phone);
				}

			} catch (JSONException e) {
				// @TODO: handle error
				e.printStackTrace();
				return false;
			} catch (final OCSyncException e) {
				// @TODO: handle error
				e.printStackTrace();
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
