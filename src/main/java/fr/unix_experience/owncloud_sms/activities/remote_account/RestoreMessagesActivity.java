package fr.unix_experience.owncloud_sms.activities.remote_account;

/*
 *  Copyright (c) 2014-2016, Loic Blot <loic.blot@unix-experience.fr>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSRecovery;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;

public class RestoreMessagesActivity extends AppCompatActivity {

	Account _account = null;
	String _defaultSmsApp;
	private static final int REQUEST_DEFAULT_SMSAPP = 1;
	boolean restoreInProgress = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_messages);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		assert getIntent().getExtras() != null;

		String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;
		AccountManager accountManager = AccountManager.get(getBaseContext());
		Account[] accountList = accountManager.getAccountsByType(getString(R.string.account_type));
		for (Account element : accountList) {
			if (element.name.equals(accountName)) {
				_account = element;
			}
		}

		if (_account == null) {
			throw new IllegalStateException(getString(R.string.err_didnt_find_account_restore));
		}

		initInterface();
		Button fix_button = (Button) findViewById(R.id.button_fix_permissions);
		final Button launch_restore = (Button) findViewById(R.id.button_launch_restore);
		final ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar_restore);

		final RestoreMessagesActivity me = this;
		fix_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
					notifyIncompatibleVersion();
					return;
				}

				if (!new ConnectivityMonitor(me).isValid()) {
					notifyNoConnectivity();
					return;
				}

				Log.i(RestoreMessagesActivity.TAG, "Ask to change the default SMS app");

				Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
				intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getBaseContext().getPackageName());
				startActivityForResult(intent, REQUEST_DEFAULT_SMSAPP);
			}
		});

		launch_restore.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!new ConnectivityMonitor(me).isValid()) {
					notifyNoConnectivity();
					return;
				}

				launch_restore.setVisibility(View.INVISIBLE);
				pb.setVisibility(View.VISIBLE);

				// Verify connectivity
				Log.i(RestoreMessagesActivity.TAG, "Launching restore asynchronously");
				restoreInProgress = true;
				new ASyncSMSRecovery.SMSRecoveryTask(me, _account).execute();
			}
		});
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval = true;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                retval = super.onOptionsItemSelected(item);
        }
        return retval;
    }

	private void initInterface() {
		TextView tv_error = (TextView) findViewById(R.id.tv_error_default_smsapp);
		tv_error.setText(R.string.error_make_default_sms_app);
		findViewById(R.id.tv_restore_finished).setVisibility(View.INVISIBLE);
		findViewById(R.id.tv_progress_value).setVisibility(View.INVISIBLE);
		Button fix_button = (Button) findViewById(R.id.button_fix_permissions);
		Button launch_restore = (Button) findViewById(R.id.button_launch_restore);
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar_restore);
		pb.setVisibility(View.INVISIBLE);

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			notifyIncompatibleVersion();
			return;
		}

		_defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
		if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
			_defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getBaseContext());
			tv_error.setVisibility(View.VISIBLE);
			fix_button.setVisibility(View.VISIBLE);
			launch_restore.setVisibility(View.INVISIBLE);
		}
		else {
			tv_error.setVisibility(View.INVISIBLE);
			fix_button.setVisibility(View.INVISIBLE);
			launch_restore.setVisibility(View.VISIBLE);
		}
	}

	private void errorNotification(int err) {
		TextView tv = (TextView) findViewById(R.id.tv_error_default_smsapp);
		Button fix_button = (Button) findViewById(R.id.button_fix_permissions);
		Button launch_restore = (Button) findViewById(R.id.button_launch_restore);
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar_restore);
		tv.setText(err);
		tv.setVisibility(View.VISIBLE);
		fix_button.setVisibility(View.INVISIBLE);
		launch_restore.setVisibility(View.INVISIBLE);
		pb.setVisibility(View.INVISIBLE);
	}

	private void notifyIncompatibleVersion() {
		errorNotification(R.string.err_kitkat_required);
	}

	private void notifyNoConnectivity() {
		errorNotification(R.string.err_no_connection);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RestoreMessagesActivity.REQUEST_DEFAULT_SMSAPP:
				if (resultCode == Activity.RESULT_OK) {
					TextView tv = (TextView) findViewById(R.id.tv_error_default_smsapp);
					Button fix_button = (Button) findViewById(R.id.button_fix_permissions);
					Button launch_restore = (Button) findViewById(R.id.button_launch_restore);
					tv.setVisibility(View.INVISIBLE);
					fix_button.setVisibility(View.INVISIBLE);
					launch_restore.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!new ConnectivityMonitor(this).isValid()) {
			notifyNoConnectivity();
			return;
		}

		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			notifyIncompatibleVersion();
			return;
		}

		if (!restoreInProgress) {
			initInterface();
		}
	}

	public void onRestoreDone() {
		findViewById(R.id.progressbar_restore).setVisibility(View.INVISIBLE);
		findViewById(R.id.tv_restore_finished).setVisibility(View.VISIBLE);

		Intent finalIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
		finalIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, _defaultSmsApp);
		startActivity(finalIntent);

		restoreInProgress = false;
	}

	public void onProgressUpdate(Integer value) {
		TextView tv_progress = (TextView) findViewById(R.id.tv_progress_value);
		if (tv_progress.getVisibility() == View.INVISIBLE) {
			tv_progress.setVisibility(View.VISIBLE);
		}

		tv_progress.setText(value.toString() + " " + getString(R.string.x_messages_restored));
	}

	private static final String TAG = RestoreMessagesActivity.class.getSimpleName();
}
