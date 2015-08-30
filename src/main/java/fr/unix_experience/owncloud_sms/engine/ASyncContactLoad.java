package fr.unix_experience.owncloud_sms.engine;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;

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
		private SwipeRefreshLayout _layout;
		private ProgressBar _pg;
		private Spinner _contactSpinner;

		public ContactLoadTask(final Account account, final Context context,
				ContactListAdapter adapter, ArrayList<String> objects, SwipeRefreshLayout layout,
				ProgressBar pg, Spinner sp) {
			if (_accountMgr == null) {
				_accountMgr = AccountManager.get(context);
			}

			_account = account;
			_context = context;
			_adapter = adapter;
			_objects = objects;
			_layout = layout;
			_pg = pg;
			_contactSpinner = sp;
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

				ArrayList<String> serverPhoneList = new ArrayList<>();

				JSONArray phoneNumbers = _client.getServerPhoneNumbers();
				for (int i = 0; i < phoneNumbers.length(); i++) {
					String phone = phoneNumbers.getString(i);
					serverPhoneList.add(phone);
				}

				// Read all contacts
				ContentResolver cr = _context.getContentResolver();
				Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
						null, null, null, null);
				if (cur.getCount() > 0) {
					while (cur.moveToNext()) {
						String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
						String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						if (Integer.parseInt(cur.getString(
								cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

							// Fetch all phone numbers
							Cursor pCur = cr.query(
									ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
									null,
									ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
									new String[]{id}, null);
							while (pCur.moveToNext()) {
								String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								phoneNo = phoneNo.replaceAll(" ", "");
								if (serverPhoneList.contains(phoneNo)) {
									if (!_objects.contains(name)) {
										_objects.add(name);
									}
									serverPhoneList.remove(phoneNo);
								}
							}
							pCur.close();
						}
					}
				}
				cur.close();

				for (String phone : serverPhoneList) {
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
			_layout.setRefreshing(false);
			if (_pg != null) {
				_pg.setVisibility(View.INVISIBLE);
			}

			if (_contactSpinner != null) {
				_contactSpinner.setVisibility(View.VISIBLE);
			}
		}
	}

	static final String TAG = ASyncContactLoad.class.getSimpleName();
}
