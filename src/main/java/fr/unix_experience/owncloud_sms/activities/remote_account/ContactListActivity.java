package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Vector;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.adapters.ContactListAdapter;
import fr.unix_experience.owncloud_sms.adapters.RecoveryPhoneNumberListViewAdapter;
import fr.unix_experience.owncloud_sms.engine.ASyncContactLoad;

public class ContactListActivity extends Activity implements ASyncContactLoad {

	static AccountManager _accountMgr;
	ContactListAdapter adapter;
	SwipeRefreshLayout _layout;
	ArrayList<String> objects;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		assert getIntent().getExtras() != null;

		String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;

        ContactListActivity._accountMgr = AccountManager.get(getBaseContext());
		Account[] myAccountList =
                ContactListActivity._accountMgr.getAccountsByType(getString(R.string.account_type));

		// Init view
		objects = new ArrayList<>();
		setContentView(R.layout.restore_activity_contactlist);

		_layout = (SwipeRefreshLayout) findViewById(R.id.contactlist_swipe_container);

		_layout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		adapter = new ContactListAdapter(getBaseContext(), objects);

		final Spinner sp = (Spinner) findViewById(R.id.contact_spinner);
		final LinearLayout contactInfos = (LinearLayout) findViewById(R.id.contactinfos_layout);
		final ProgressBar contactProgressBar = (ProgressBar) findViewById(R.id.contactlist_pgbar);
        final ListView contactPhoneListView = (ListView) findViewById(R.id.contact_phonelistView);
        final RecoveryPhoneNumberListViewAdapter contactPhoneListAdapter =
                new RecoveryPhoneNumberListViewAdapter(getBaseContext());
        contactPhoneListView.setAdapter(contactPhoneListAdapter);

		contactInfos.setVisibility(View.INVISIBLE);

		sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				contactInfos.setVisibility(View.INVISIBLE);
                contactPhoneListAdapter.clear();

				String contactName = sp.getSelectedItem().toString();
				Vector<String> phoneList = fetchContact(contactName);
				Integer smsCount = 0;
				// @TODO asynctask to load more datas

				if (!phoneList.isEmpty()) {
					for (String pn: phoneList) {
                        contactPhoneListAdapter.add(pn);
					}
				} else {
                    contactPhoneListAdapter.add(contactName);
				}

				contactInfos.setVisibility(View.VISIBLE);
                contactPhoneListAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Nothing to do there
			}

			private Vector<String> fetchContact(String name) {
				Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
						new String[]{name}, null);
                if (people == null) {
                    return new Vector<>();
                }

                people.moveToFirst();

                Vector<String> r = new Vector<>();
				if (people.getCount() == 0) {
					return r;
				}

				String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));

				if ("1".equalsIgnoreCase(people.getString(people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))) {
					Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
							new String[]{contactId}, null);

					while ((phones != null) && phones.moveToNext()) {
                        r.add(phones.getString(phones.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                                .replaceAll(" ", ""));
					}

                    if (phones != null) {
                        phones.close();
                    }
                }
				return r;
			}
		});
		sp.setAdapter(adapter);

		for (final Account element : myAccountList) {
			if (element.name.equals(accountName)) {
				// Load "contacts"
				contactProgressBar.setVisibility(View.VISIBLE);
				new ContactLoadTask(element, getBaseContext(), adapter, objects, _layout, contactProgressBar, contactInfos).execute();

				// Add refresh handler
				_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						_layout.setRefreshing(true);
						contactInfos.setVisibility(View.INVISIBLE);
						contactProgressBar.setVisibility(View.VISIBLE);
						(new Handler()).post(new Runnable() {
							@Override
							public void run() {
								objects.clear();
								adapter.notifyDataSetChanged();
								new ContactLoadTask(element, getBaseContext(), adapter, objects, _layout, contactProgressBar, contactInfos).execute();
							}
						});
					}
				});
				return;
			}
		}
	}
}
