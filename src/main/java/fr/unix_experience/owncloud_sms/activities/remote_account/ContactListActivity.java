package fr.unix_experience.owncloud_sms.activities.remote_account;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;
import fr.nrz.androidlib.adapters.AndroidAccountAdapter;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.adapters.ContactListAdapter;
import fr.unix_experience.owncloud_sms.engine.ASyncContactLoad;

public class ContactListActivity extends ListActivity implements ASyncContactLoad {

	static AccountManager _accountMgr;
	ContactListAdapter adapter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		assert getIntent().getExtras() != null;

		final String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;

		_accountMgr = AccountManager.get(getBaseContext());
		final Account[] myAccountList =
				_accountMgr.getAccountsByType(getString(R.string.account_type));
		
		// Init view
		ArrayList<String> objects = new ArrayList<String>();
		setContentView(R.layout.restore_activity_contactlist);
		adapter = new ContactListAdapter(getBaseContext(),
				android.R.layout.simple_list_item_1,
				objects,
				R.layout.contact_list_item,
				R.id.contactname);
		
		setListAdapter(adapter);

		for (final Account element : myAccountList) {
			if (element.name.equals(accountName)) {
				new ContactLoadTask(element, getBaseContext(), adapter, objects).execute();
				return;
			}
		}
	}

	// This function fetch contacts from the ownCloud instance and generate the list activity
	private void loadContacts(final Account account) {

	}
}
