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
import android.content.ContentResolver;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.preference.ListPreference;
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

		// Bind our boolean preferences
		_boolPrefs.add(new BindObjectPref("sync_wifi", DefaultPrefs.syncWifi));
		_boolPrefs.add(new BindObjectPref("sync_4g", DefaultPrefs.sync4G));
		_boolPrefs.add(new BindObjectPref("sync_3g", DefaultPrefs.sync3G));
		_boolPrefs.add(new BindObjectPref("sync_gprs", DefaultPrefs.syncGPRS));
		_boolPrefs.add(new BindObjectPref("sync_2g", DefaultPrefs.sync2G));
		_boolPrefs.add(new BindObjectPref("sync_others", DefaultPrefs.syncOthers));

		// Bind our string preferences
		_stringPrefs.add(new BindObjectPref("sync_frequency", ""));

		// Must be at the end, after preference bind
		super.onPostCreate(savedInstanceState);
	}

	protected static void handleCheckboxPreference(final String key, final Boolean value) {
		// Network types allowed for sync
		if(key.equals(new String("sync_wifi")) || key.equals("sync_2g") ||
				key.equals(new String("sync_3g")) || key.equals("sync_gprs") ||
				key.equals("sync_4g") || key.equals("sync_others")) {
			final OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(_context);
			prefs.putBoolean(key, value);
		}
		else {
			// Unknown option
		}
	}

	protected static void handleListPreference(final String key, final String value,
			final ListPreference preference) {
		// For list preferences, look up the correct display value in
		// the preference's 'entries' list.
		final int index = preference.findIndexOfValue(value);

		// Set the summary to reflect the new value.
		preference
		.setSummary(index >= 0 ? preference.getEntries()[index]
				: null);

		// Handle sync frequency change
		if (key.equals("sync_frequency")) {
			final Account[] myAccountList = _accountMgr.getAccountsByType(_accountType);
			final long syncFreq = Long.parseLong(value);

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
		else {
			// Unhandled option
		}
	}
}
