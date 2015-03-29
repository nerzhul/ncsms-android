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

import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.PeriodicSync;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import fr.nrz.androidlib.activities.NrzSettingsActivity;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class GeneralSettingsActivity extends NrzSettingsActivity {
	private static AccountManager _accountMgr;
	private static String _accountAuthority;
	private static String _accountType;

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		_accountMgr = AccountManager.get(getBaseContext());
		_accountAuthority = getString(R.string.account_authority);
		_accountType = getString(R.string.account_type);
		_prefsRessourceFile = R.xml.pref_data_sync;
		sBindPreferenceListener = sBindPreferenceSummaryToValueListener;
		super.onPostCreate(savedInstanceState);
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(final Preference preference, final Object value) {
			if (preference instanceof ListPreference) {
				final String prefKey = preference.getKey();
				final String stringValue = value.toString();
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				final ListPreference listPreference = (ListPreference) preference;
				final int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
				.setSummary(index >= 0 ? listPreference.getEntries()[index]
						: null);

				final Account[] myAccountList = _accountMgr.getAccountsByType(_accountType);

				// Handle sync frequency change
				if (prefKey.equals("sync_frequency")) {
					final long syncFreq = Long.parseLong(stringValue);

					// Get ownCloud SMS account list
					for (int i = 0; i < myAccountList.length; i++) {
						// And get all authorities for this account
						final List<PeriodicSync> syncList = ContentResolver.getPeriodicSyncs(myAccountList[i], _accountAuthority);

						boolean foundSameSyncCycle = false;
						for (int j = 0; j < syncList.size(); j++) {
							final PeriodicSync ps = syncList.get(i);

							if (ps.period == syncFreq && ps.extras.getInt("synctype") == 1) {
								foundSameSyncCycle = true;
							}
						}

						if (foundSameSyncCycle == false) {
							final Bundle b = new Bundle();
							b.putInt("synctype", 1);

							ContentResolver.removePeriodicSync(myAccountList[i],
									_accountAuthority, b);
							ContentResolver.addPeriodicSync(myAccountList[i],
									_accountAuthority, b, syncFreq * 60);
						}
					}
				}
			} else if (preference instanceof CheckBoxPreference) {
				final String prefKey = preference.getKey();
				final Boolean boolValue = (Boolean)value;
				// Network types allowed for sync
				if(prefKey.equals(new String("sync_wifi")) || prefKey.equals("sync_2g") ||
						prefKey.equals(new String("sync_3g")) || prefKey.equals("sync_gprs") ||
						prefKey.equals("sync_4g") || prefKey.equals("sync_others")) {

					final OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(_context);
					prefs.putBoolean(prefKey, boolValue);
				}
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				//preference.setSummary(boolValue);
			}
			return true;
		}
	};

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(_prefsRessourceFile);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceStringToValue(findPreference("sync_frequency"));
			bindPreferenceBooleanToValue(findPreference("sync_wifi"), DefaultPrefs.syncWifi);
			bindPreferenceBooleanToValue(findPreference("sync_4g"), DefaultPrefs.sync4G);
			bindPreferenceBooleanToValue(findPreference("sync_3g"), DefaultPrefs.sync3G);
			bindPreferenceBooleanToValue(findPreference("sync_gprs"), DefaultPrefs.syncGPRS);
			bindPreferenceBooleanToValue(findPreference("sync_2g"), DefaultPrefs.sync2G);
			bindPreferenceBooleanToValue(findPreference("sync_others"), DefaultPrefs.syncOthers);
		}
	}

	@Override
	protected void bindPreferences() {
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceStringToValue(findPreference("sync_frequency"));
		bindPreferenceBooleanToValue(findPreference("sync_wifi"), DefaultPrefs.syncWifi);
		bindPreferenceBooleanToValue(findPreference("sync_4g"), DefaultPrefs.sync4G);
		bindPreferenceBooleanToValue(findPreference("sync_3g"), DefaultPrefs.sync3G);
		bindPreferenceBooleanToValue(findPreference("sync_gprs"), DefaultPrefs.syncGPRS);
		bindPreferenceBooleanToValue(findPreference("sync_2g"), DefaultPrefs.sync2G);
		bindPreferenceBooleanToValue(findPreference("sync_others"), DefaultPrefs.syncOthers);
	}
}
