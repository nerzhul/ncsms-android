package fr.unix_experience.owncloud_sms.activities.remote_account;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;

import fr.nrz.androidlib.adapters.AndroidAccountAdapter;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.adapters.ContactListAdapter;
import fr.unix_experience.owncloud_sms.engine.ASyncContactLoad;

public class ContactListActivity extends Activity implements ASyncContactLoad {

	static AccountManager _accountMgr;
	ContactListAdapter adapter;
	SwipeRefreshLayout _layout;
	ArrayList<String> objects;

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
		objects = new ArrayList<String>();
		setContentView(R.layout.restore_activity_contactlist);

		_layout = (SwipeRefreshLayout) findViewById(R.id.contactlist_swipe_container);

		_layout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		
		adapter = new ContactListAdapter(getBaseContext(),
				android.R.layout.simple_spinner_item,
				objects,
				R.layout.contact_list_item,
				R.id.contactname, this);
		
		final Spinner sp = (Spinner) findViewById(R.id.contact_spinner);
		sp.setVisibility(View.INVISIBLE);
		sp.setAdapter(adapter);

		final ProgressBar contactProgressBar = (ProgressBar) findViewById(R.id.contactlist_pgbar);

		for (final Account element : myAccountList) {
			if (element.name.equals(accountName)) {
				// Load "contacts"
				contactProgressBar.setVisibility(View.VISIBLE);
				sp.setVisibility(View.INVISIBLE);
				new ContactLoadTask(element, getBaseContext(), adapter, objects, _layout, contactProgressBar, sp).execute();

				// Add refresh handler
				_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						_layout.setRefreshing(true);
						sp.setVisibility(View.INVISIBLE);
						contactProgressBar.setVisibility(View.VISIBLE);
						(new Handler()).post(new Runnable() {
							@Override
							public void run() {
								objects.clear();
								adapter.notifyDataSetChanged();
								new ContactLoadTask(element, getBaseContext(), adapter, objects, _layout, contactProgressBar, sp).execute();
							}
						});
					}
				});
				return;
			}
		}
	}
}
