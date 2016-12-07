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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.ASyncSMSRecovery;

public class RestoreMessagesActivity extends AppCompatActivity {

	Account _account = null;
	String _defaultSmsApp;
	private static final int REQUEST_DEFAULT_SMSAPP = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_messages);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
			// @TODO Change message to define Android 4.4 or greated is required
			return;
		}

		assert getIntent().getExtras() != null;

		String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;
		AccountManager accountManager = AccountManager.get(getBaseContext());
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		Account[] accountList = accountManager.getAccountsByType(getString(R.string.account_type));
		for (Account element : accountList) {
			if (element.name.equals(accountName)) {
				_account = element;
			}
		}

		if (_account == null) {
			throw new IllegalStateException(getString(R.string.err_didnt_find_account_restore));
		}

		_defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
		TextView tv = (TextView) findViewById(R.id.tv_error_default_smsapp);
		Button fix_button = (Button) findViewById(R.id.button_fix_permissions);
		final Button launch_restore = (Button) findViewById(R.id.button_launch_restore);
		final ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar_restore);
		pb.setVisibility(View.INVISIBLE);

		if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
			_defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getBaseContext());
			tv.setVisibility(View.VISIBLE);
			fix_button.setVisibility(View.VISIBLE);
			launch_restore.setVisibility(View.INVISIBLE);
		}
		else {
			tv.setVisibility(View.INVISIBLE);
			fix_button.setVisibility(View.INVISIBLE);
			launch_restore.setVisibility(View.VISIBLE);
		}

		fix_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
					// @TODO Change message to define Android 4.4 or greated is required
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
				launch_restore.setVisibility(View.INVISIBLE);
				pb.setVisibility(View.VISIBLE);
				Log.i(RestoreMessagesActivity.TAG, "Launching restore asynchronously");
				new ASyncSMSRecovery.SMSRecoveryTask(getApplicationContext(), _account).execute();
			}
		});


		/*Intent finalIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
		finalIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, _defaultSmsApp);
		startActivity(finalIntent);*/
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RestoreMessagesActivity.REQUEST_DEFAULT_SMSAPP:
				Log.i(RestoreMessagesActivity.TAG, "RC: " + Integer.toString(resultCode));
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

	private static final String TAG = RestoreMessagesActivity.class.getSimpleName();
}
