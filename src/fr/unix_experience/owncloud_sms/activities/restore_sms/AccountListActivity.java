package fr.unix_experience.owncloud_sms.activities.restore_sms;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;
import fr.nrz.androidlib.adapters.AndroidAccountAdapter;
import fr.unix_experience.owncloud_sms.R;

public class AccountListActivity extends ListActivity {
	ArrayList<Account> listItems = new ArrayList<Account>();
	AndroidAccountAdapter adapter;

	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		final AccountManager _accountMgr = AccountManager.get(getBaseContext());

		setContentView(R.layout.restore_activity_accountlist);
		adapter = new AndroidAccountAdapter(this,
				android.R.layout.simple_list_item_1,
				listItems,
				R.layout.account_list_item,
				R.id.accountname, ContactListActivity.class);
		setListAdapter(adapter);

		final Account[] accountList =
				_accountMgr.getAccountsByType(getString(R.string.account_type));
		for (final Account element : accountList) {
			listItems.add(element);
		}

		adapter.notifyDataSetChanged();
	}
}

