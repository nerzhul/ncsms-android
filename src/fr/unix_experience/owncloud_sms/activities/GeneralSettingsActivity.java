package fr.unix_experience.owncloud_sms.activities;

/*
 *  Copyright (c) 2014, Loic Blot <loic.blot@unix-experience.fr>
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
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import java.util.List;

import fr.unix_experience.owncloud_sms.R;

public class GeneralSettingsActivity extends PreferenceActivity {
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	static AccountManager mAccountMgr;
	static String mAccountAuthority;
	static String mSlowSyncAccountAuthority;
	static String mAccountType;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mAccountMgr = AccountManager.get(getBaseContext());
		mAccountAuthority = getString(R.string.account_authority);
		mSlowSyncAccountAuthority = getString(R.string.slowsync_account_authority);
		mAccountType = getString(R.string.account_type);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		addPreferencesFromResource(R.xml.pref_data_sync);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("sync_frequency"));
		//bindPreferenceSummaryToValue(findPreference("slow_sync_frequency"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			
			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
					.setSummary(index >= 0 ? listPreference.getEntries()[index]
					: null);
				
				
				String prefKey = preference.getKey();
				
				// Handle sync frequency change
				if (prefKey.equals(new String("sync_frequency"))) {
					long syncFreq = Long.parseLong((String)value);

					// Get ownCloud SMS account list
					Account[] myAccountList = mAccountMgr.getAccountsByType(mAccountType);
					for (int i = 0; i < myAccountList.length; i++) {
						// And get all authorities for this account
						List<PeriodicSync> syncList = ContentResolver.getPeriodicSyncs(myAccountList[i], mAccountAuthority);
						
						boolean foundSameSyncCycle = false;
						for (int j = 0; j < syncList.size(); j++) {
							PeriodicSync ps = syncList.get(i);
							
							if (ps.period == syncFreq && ps.extras.getInt("synctype") == 1) {
								foundSameSyncCycle = true;
							}
						}
						
						if (foundSameSyncCycle == false) {
							Bundle b = new Bundle();
							b.putInt("synctype", 1);

							ContentResolver.removePeriodicSync(myAccountList[i], 
									mAccountAuthority, b);
							ContentResolver.addPeriodicSync(myAccountList[i],
				                mAccountAuthority, b, syncFreq * 60);
						}
					}
				// Slow Sync frequency 
				} /*else if (prefKey.equals(new String("slow_sync_frequency"))) {
					long syncFreq = Long.parseLong((String)value);

					// Get ownCloud SMS account list
					Account[] myAccountList = mAccountMgr.getAccountsByType(mAccountType);
					for (int i = 0; i < myAccountList.length; i++) {
						// And get all authorities for this account
						List<PeriodicSync> syncList = ContentResolver.getPeriodicSyncs(myAccountList[i], mSlowSyncAccountAuthority);
						
						boolean foundSameSyncCycle = false;
						for (int j = 0; j < syncList.size(); j++) {
							PeriodicSync ps = syncList.get(i);
							
							if (ps.period == syncFreq && ps.extras.getInt("synctype") == 2) {
								foundSameSyncCycle = true;
							}
						}
						
						if (foundSameSyncCycle == false) {
							Bundle b = new Bundle();
							b.putInt("synctype", 2);

							ContentResolver.removePeriodicSync(myAccountList[i], 
								mSlowSyncAccountAuthority, b);
							ContentResolver.addPeriodicSync(myAccountList[i],
								mSlowSyncAccountAuthority, b, syncFreq * 60);
						}
					}
				}*/
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
			preference,
			PreferenceManager.getDefaultSharedPreferences(
				preference.getContext()).getString(
				preference.getKey(),
				""
			)
		);
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);

			// Bind the summaries of EditText/List/Dialog/Ringtone preferences
			// to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design
			// guidelines.
			bindPreferenceSummaryToValue(findPreference("sync_frequency"));
			//bindPreferenceSummaryToValue(findPreference("slow_sync_frequency"));
		}
	}
}
