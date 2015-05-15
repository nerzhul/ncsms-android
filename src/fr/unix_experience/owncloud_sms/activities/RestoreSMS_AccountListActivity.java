package fr.unix_experience.owncloud_sms.activities;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;
import fr.nrz.androidlib.adapters.AndroidAccountAdapter;
import fr.unix_experience.owncloud_sms.R;

public class RestoreSMS_AccountListActivity extends ListActivity {
	ArrayList<Account> listItems = new ArrayList<Account>();
	AndroidAccountAdapter adapter;

	private static String _accountType;
	private static AccountManager _accountMgr;

	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		_accountType = getString(R.string.account_type);
		_accountMgr = AccountManager.get(getBaseContext());

		setContentView(R.layout.restore_activity_accountlist);
		adapter = new AndroidAccountAdapter(this,
				android.R.layout.simple_list_item_1,
				listItems,
				R.layout.account_list_item,
				R.id.accountname, RestoreSMS_ContactListActivity.class);
		setListAdapter(adapter);

		final Account[] myAccountList = _accountMgr.getAccountsByType(_accountType);
		for (final Account element : myAccountList) {
			listItems.add(element);
		}

		adapter.notifyDataSetChanged();
	}
}

