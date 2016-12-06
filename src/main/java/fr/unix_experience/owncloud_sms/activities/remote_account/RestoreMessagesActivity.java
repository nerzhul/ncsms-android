package fr.unix_experience.owncloud_sms.activities.remote_account;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import fr.unix_experience.owncloud_sms.R;

public class RestoreMessagesActivity extends AppCompatActivity {

	Account _account = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restore_messages);

		assert getIntent().getExtras() != null;

		String accountName = getIntent().getExtras().getString("account");

		// accountName cannot be null, devel error
		assert accountName != null;
		AccountManager accountManager = AccountManager.get(getBaseContext());
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
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
	}

	private static final String TAG = RestoreMessagesActivity.class.getSimpleName();
}
