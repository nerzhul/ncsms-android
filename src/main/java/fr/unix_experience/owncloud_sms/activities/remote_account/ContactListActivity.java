package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.adapters.ContactListAdapter;
import fr.unix_experience.owncloud_sms.adapters.RecoveryPhoneNumberListViewAdapter;
import fr.unix_experience.owncloud_sms.engine.ASyncContactLoad;
import fr.unix_experience.owncloud_sms.enums.PermissionID;
import fr.unix_experience.owncloud_sms.prefs.PermissionChecker;

import static fr.unix_experience.owncloud_sms.enums.PermissionID.REQUEST_CONTACTS;
import static fr.unix_experience.owncloud_sms.enums.PermissionID.REQUEST_MAX;

public class ContactListActivity extends AppCompatActivity implements ASyncContactLoad {

	static AccountManager mAccountMgr;
	ContactListAdapter mAdapter = null;
	SwipeRefreshLayout mLayout = null;
    LinearLayout mContactInfos = null;
	ArrayList<String> mObjects;
    String mFetchedContact;
    RecoveryPhoneNumberListViewAdapter mContactPhoneListAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		assert getIntent().getExtras() != null;

		String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;

        ContactListActivity.mAccountMgr = AccountManager.get(getBaseContext());
		Account[] myAccountList =
                ContactListActivity.mAccountMgr.getAccountsByType(getString(R.string.account_type));

		// Init view
		mObjects = new ArrayList<>();
		setContentView(R.layout.restore_activity_contactlist);

		mLayout = (SwipeRefreshLayout) findViewById(R.id.contactlist_swipe_container);

		mAdapter = new ContactListAdapter(getBaseContext(), mObjects);

		final Spinner sp = (Spinner) findViewById(R.id.contact_spinner);
		mContactInfos = (LinearLayout) findViewById(R.id.contactinfos_layout);
		final ProgressBar contactProgressBar = (ProgressBar) findViewById(R.id.contactlist_pgbar);
        ListView contactPhoneListView = (ListView) findViewById(R.id.contact_phonelistView);
        mContactPhoneListAdapter = new RecoveryPhoneNumberListViewAdapter(getBaseContext());
        assert contactPhoneListView != null;
        contactPhoneListView.setAdapter(mContactPhoneListAdapter);

		mContactInfos.setVisibility(View.INVISIBLE);

        assert sp != null;
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mContactInfos.setVisibility(View.INVISIBLE);
                mContactPhoneListAdapter.clear();

                mFetchedContact = sp.getSelectedItem().toString();
				fetchContact(mFetchedContact);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Nothing to do there
			}


		});
		sp.setAdapter(mAdapter);

		for (final Account element : myAccountList) {
			if (element.name.equals(accountName)) {
				// Load "contacts"
                assert contactProgressBar != null;
                contactProgressBar.setVisibility(View.VISIBLE);
				new ContactLoadTask(element, getBaseContext(), mAdapter, mObjects, mLayout, contactProgressBar, mContactInfos).execute();

				// Add refresh handler
				mLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mLayout.setRefreshing(true);
                        mContactInfos.setVisibility(View.INVISIBLE);
                        contactProgressBar.setVisibility(View.VISIBLE);
                        (new Handler()).post(new Runnable() {
                            @Override
                            public void run() {
                                mObjects.clear();
                                mAdapter.notifyDataSetChanged();
                                new ContactLoadTask(element, getBaseContext(), mAdapter, mObjects, mLayout, contactProgressBar, mContactInfos).execute();
                            }
                        });
                    }
                });
				return;
			}
		}
	}

    private void fetchContact(String name) {

        if (!PermissionChecker.checkPermission(this, Manifest.permission.READ_CONTACTS,
                REQUEST_CONTACTS)) {
            return;
        }

        Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{name}, null);
        if (people == null) {
            return;
        }

        people.moveToFirst();

        Vector<String> r = new Vector<>();
        if (people.getCount() == 0) {
            return;
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

        Integer smsCount = 0;
        // @TODO asynctask to load more datas

        if (!r.isEmpty()) {
            for (String pn: r) {
                mContactPhoneListAdapter.add(pn);
            }
        } else {
            mContactPhoneListAdapter.add(mFetchedContact);
        }

        mContactInfos.setVisibility(View.VISIBLE);
        mContactPhoneListAdapter.notifyDataSetChanged();
    }

    /*
     * Permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        PermissionID requestCodeID = REQUEST_MAX;
        if ((requestCode > 0) || (requestCode < REQUEST_MAX.ordinal())) {
            requestCodeID = PermissionID.values()[requestCode];
        }
        switch (requestCodeID) {
            case REQUEST_CONTACTS:
                for (int grantResult : grantResults) {
                    Log.i("OcSMS", Integer.toString(grantResult));
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchContact(mFetchedContact);
                } else {
                    // Permission Denied
                    Toast.makeText(this, getString(R.string.err_cannot_read_contacts) + " " +
                            getString(R.string.please_fix_it), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
