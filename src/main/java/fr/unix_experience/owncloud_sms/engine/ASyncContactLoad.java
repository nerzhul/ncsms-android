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
		private final ContactListAdapter _adapter;
		private final ArrayList<String> _objects;
		private final SwipeRefreshLayout _layout;
		private final ProgressBar _pg;
		private final Spinner _contactSpinner;

		public ContactLoadTask(Account account, Context context,
				ContactListAdapter adapter, ArrayList<String> objects, SwipeRefreshLayout layout,
				ProgressBar pg, Spinner sp) {
			if (ContactLoadTask._accountMgr == null) {
                ContactLoadTask._accountMgr = AccountManager.get(context);
			}

            ContactLoadTask._account = account;
			_context = context;
			_adapter = adapter;
			_objects = objects;
			_layout = layout;
			_pg = pg;
			_contactSpinner = sp;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			// Create client
			String ocURI = ContactLoadTask._accountMgr.getUserData(ContactLoadTask._account, "ocURI");
			if (ocURI == null) {
				// @TODO: Handle the problem
				return false;
			}

			Uri serverURI = Uri.parse(ocURI);

			OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(_context,
					serverURI, ContactLoadTask._accountMgr.getUserData(ContactLoadTask._account, "ocLogin"),
                    ContactLoadTask._accountMgr.getPassword(ContactLoadTask._account));

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
				if (((cur != null) ? cur.getCount() : 0) > 0) {
                    String id, name;
					while ((cur != null) && cur.moveToNext()) {
						id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
						name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						if (Integer.parseInt(cur.getString(
								cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

							// Fetch all phone numbers
							Cursor pCur = cr.query(
									ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
									null,
									ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
									new String[]{id}, null);
							while ((pCur != null) && pCur.moveToNext()) {
								String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
										.replaceAll(" ", "");
								if (serverPhoneList.contains(phoneNo)) {
									if (!_objects.contains(name)) {
										_objects.add(name);
									}
									serverPhoneList.remove(phoneNo);
								}
							}
                            if (pCur != null) {
                                pCur.close();
                            }
                        }
					}
				}
                if (cur != null) {
                    cur.close();
                }

                for (String phone : serverPhoneList) {
					_objects.add(phone);
				}

				// Sort phone numbers
				Collections.sort(_objects);
			} catch (JSONException e) {
				_objects.add(_context.getString(R.string.err_fetch_phonelist));
				return false;
			} catch (OCSyncException e) {
				_objects.add(_context.getString(e.getErrorId()));
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean success) {
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

	String TAG = ASyncContactLoad.class.getSimpleName();
}
