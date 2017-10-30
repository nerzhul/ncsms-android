package fr.unix_experience.owncloud_sms.activities;

/*
 *  Copyright (c) 2014-2015, Loic Blot <loic.blot@unix-experience.fr>
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;

import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;
import fr.unix_experience.owncloud_sms.engine.OCHttpClient;

/**
 * A login screen that offers login via email/password.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoginActivity extends AppCompatActivity {

	private static final String TAG = LoginActivity.class.getCanonicalName();

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private Spinner _protocolView;
	private EditText _loginView;
	private EditText _passwordView;
	private EditText _serverView;
	private ActionProcessButton _signInButton;
	private View mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Set up the login form.
		_protocolView = (Spinner) findViewById(R.id.oc_protocol);
		_serverView = (EditText) findViewById(R.id.oc_server);
		_loginView = (EditText) findViewById(R.id.oc_login);

		_passwordView = (EditText) findViewById(R.id.oc_password);
		_passwordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
												  KeyEvent keyEvent) {
						if ((id == R.id.oc_login) || (id == EditorInfo.IME_NULL)) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		_signInButton = (ActionProcessButton) findViewById(R.id.oc_signin_button);
		_signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
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

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		_loginView.setError(null);
		_passwordView.setError(null);

		// Store values at the time of the login attempt.
		String protocol = _protocolView.getSelectedItem().toString();
		String login = _loginView.getText().toString();
		String password = _passwordView.getText().toString();
		String serverAddr = _serverView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid server address.
		if (TextUtils.isEmpty(protocol)) {
			cancel = true;
		}

		// Check for a valid server address.
		if (TextUtils.isEmpty(serverAddr)) {
			_serverView.setError(getString(R.string.error_field_required));
			focusView = _loginView;
			cancel = true;
		}

		// Check for a valid login address.
		if (TextUtils.isEmpty(login)) {
			_loginView.setError(getString(R.string.error_field_required));
			focusView = _loginView;
			cancel = true;
		}

		// Check for a valid password
		if (TextUtils.isEmpty(password)) {
			_passwordView.setError(getString(R.string.error_field_required));
			focusView = _passwordView;
			cancel = true;
		}

		if (!isPasswordValid(password)) {
			_passwordView.setError(getString(R.string.error_invalid_password));
			focusView = _passwordView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			// reset the button progress
			_signInButton.setProgress(0);
			if (focusView != null) {
				focusView.requestFocus();
			}
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			_signInButton.setProgress(25);
			showProgress(true);
			String serverURL = protocol + serverAddr;
			try {
				mAuthTask = new UserLoginTask(serverURL, login, password);
				mAuthTask.execute((Void) null);
			} catch (MalformedURLException e) {
				Log.e(TAG, "Invalid server URL " + serverURL);
			}
		}
	}

	private boolean isPasswordValid(String password) {
		// TODO: Replace this with your own logic
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		UserLoginTask(String serverURL, String login, String password) throws MalformedURLException {
			_serverURL = new URL(serverURL);
			Log.i(TAG, "_serverURL = " + serverURL);
			_login = login;
			_password = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			_returnCode = 0;
			OCHttpClient http = new OCHttpClient(getBaseContext(), _serverURL, _login, _password);
			GetMethod testMethod = null;
			try {
				testMethod = http.getVersion();
				_returnCode = http.execute(testMethod);
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "Failed to getVersion, IllegalArgumentException occured: " + e.getMessage());
				_returnCode = 597;
			} catch (IOException e) {
				Log.w(TAG, "Failed to login, IOException occured: " + e.getMessage());
				_returnCode = 599;
			}

			if (testMethod != null)
				testMethod.releaseConnection();

			return (_returnCode == 200);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mAuthTask = null;
			showProgress(false);
			_signInButton.setProgress(90);

			if (success) {
				_signInButton.setProgress(100);
				String accountType = getIntent().getStringExtra(UserLoginTask.PARAM_AUTHTOKEN_TYPE);
				if (accountType == null) {
					accountType = getString(R.string.account_type);
				}

				// Generate a label
				String accountLabel = _login + "@" + _serverURL.getHost();

				// We create the account
				Account account = new Account(accountLabel, accountType);
				Bundle accountBundle = new Bundle();
				accountBundle.putString("ocLogin", _login);
				accountBundle.putString("ocURI", _serverURL.toString());

				// And we push it to Android
				AccountManager accMgr = AccountManager.get(getApplicationContext());
				accMgr.addAccountExplicitly(account, _password, accountBundle);

				// Set sync options
				ContentResolver.setSyncAutomatically(account, getString(R.string.account_authority), true);

				Bundle b = new Bundle();
				b.putInt("synctype", 1);

				ContentResolver.addPeriodicSync(account, getString(R.string.account_authority), b, DefaultPrefs.syncInterval * 60);
				// Then it's finished
				finish();

				// Start sync settings, we have finished to configure account
				Intent settingsIntent = new Intent(Settings.ACTION_SYNC_SETTINGS);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(settingsIntent);
			} else {
				boolean serverViewRequestFocus = true;
				switch (_returnCode) {
					case 0:
						_serverView.setError("UNK");
						break;
					case 404:
						_serverView.setError(getString(R.string.error_connection_failed_not_found));
						break;
					case 597:
						_serverView.setError(getString(R.string.error_invalid_server_address));
						break;
					case 400:
					case 598:
						_serverView.setError(getString(R.string.error_connection_failed));
						break;
					case 599:
						_serverView.setError(getString(R.string.error_http_connection_failed));
						break;
					case 401:
					case 403:
						_passwordView.setError(getString(R.string.error_invalid_login));
						_passwordView.requestFocus();
						// Warning, there is no break here to disable serverViewRequestFocus too
					case 200:
					default:
						serverViewRequestFocus = false;
						break;
				}

				if (serverViewRequestFocus) {
					_serverView.requestFocus();
				}

				// If not ok, reset the progress
				if (_returnCode != 200) {
					_signInButton.setProgress(0);
				}
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

		private final URL _serverURL;
		private final String _login;
		private final String _password;
		private int _returnCode;

		static final String PARAM_AUTHTOKEN_TYPE = "auth.token";
		private final String TAG = UserLoginTask.class.getCanonicalName();
	}
}
