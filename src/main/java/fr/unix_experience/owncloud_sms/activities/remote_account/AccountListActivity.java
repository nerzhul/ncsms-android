package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;

import java.util.ArrayList;

import fr.nrz.androidlib.adapters.AndroidAccountAdapter;
import fr.unix_experience.owncloud_sms.R;

public class AccountListActivity extends ListActivity {
	ArrayList<Account> listItems = new ArrayList<>();
	AndroidAccountAdapter adapter;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		AccountManager _accountMgr = AccountManager.get(getBaseContext());

		setContentView(R.layout.restore_activity_accountlist);
        /*
		adapter = new AndroidAccountAdapter(this,
				android.R.layout.simple_list_item_1,
				listItems,
				R.layout.account_list_item,
				R.id.accountname, ContactListActivity.class);
		setListAdapter(adapter);

		Account[] accountList =
				_accountMgr.getAccountsByType(getString(R.string.account_type));
        Collections.addAll(listItems, accountList);

		adapter.notifyDataSetChanged();
        */
	}
}

