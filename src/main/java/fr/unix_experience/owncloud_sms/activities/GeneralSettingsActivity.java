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
import android.content.ContentResolver;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.Log;

import java.util.List;

import fr.nrz.androidlib.activities.NrzSettingsActivity;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class GeneralSettingsActivity extends NrzSettingsActivity {
	private static final String TAG = GeneralSettingsActivity.class.getSimpleName();

	private static AccountManager _accountMgr;
	private static String _accountAuthority;
	private static String _accountType;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
        GeneralSettingsActivity._accountMgr = AccountManager.get(getBaseContext());
        GeneralSettingsActivity._accountAuthority = getString(R.string.account_authority);
        GeneralSettingsActivity._accountType = getString(R.string.account_type);
        NrzSettingsActivity._prefsRessourceFile = R.xml.pref_data_sync;

		// Bind our boolean preferences
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("push_on_receive", DefaultPrefs.pushOnReceive));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_wifi", DefaultPrefs.syncWifi));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_4g", DefaultPrefs.sync4G));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_3g", DefaultPrefs.sync3G));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_gprs", DefaultPrefs.syncGPRS));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_2g", DefaultPrefs.sync2G));
        NrzSettingsActivity._boolPrefs.add(new BindObjectPref("sync_others", DefaultPrefs.syncOthers));

		// Bind our string preferences
        NrzSettingsActivity._stringPrefs.add(new BindObjectPref("sync_frequency", "15"));
        NrzSettingsActivity._stringPrefs.add(new BindObjectPref("sync_bulk_messages", "-1"));

		// Must be at the end, after preference bind
		super.onPostCreate(savedInstanceState);
	}

	protected void handleCheckboxPreference(String key, Boolean value) {
		// Network types allowed for sync
		if("push_on_receive".equals(key) ||
                "sync_wifi".equals(key) || "sync_2g".equals(key) ||
                "sync_3g".equals(key) || "sync_gprs".equals(key) ||
                "sync_4g".equals(key) || "sync_others".equals(key)) {
			OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(NrzSettingsActivity._context);
			Log.d(GeneralSettingsActivity.TAG,"GeneralSettingsActivity.handleCheckboxPreference: set " + key + " to "
					+ value.toString());
			prefs.putBoolean(key, value);
		}
	}

	protected void handleListPreference(String key, String value,
			ListPreference preference) {
		// For list preferences, look up the correct display value in
		// the preference's 'entries' list.
		int index = preference.findIndexOfValue(value);

		// Set the summary to reflect the new value.
		preference
		.setSummary((index >= 0) ? preference.getEntries()[index]
                : null);

        Log.d(TAG, "Modifying listPreference " + key);

        OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(NrzSettingsActivity._context);

		// Handle sync frequency change
		if ("sync_frequency".equals(key)) {
			Account[] myAccountList = GeneralSettingsActivity._accountMgr.getAccountsByType(GeneralSettingsActivity._accountType);
			long syncFreq = Long.parseLong(value);

			// Get ownCloud SMS account list
			for (int i = 0; i < myAccountList.length; i++) {
				// And get all authorities for this account
				List<PeriodicSync> syncList = ContentResolver.getPeriodicSyncs(myAccountList[i], GeneralSettingsActivity._accountAuthority);

				boolean foundSameSyncCycle = false;
				for (int j = 0; j < syncList.size(); j++) {
					PeriodicSync ps = syncList.get(i);

					if ((ps.period == syncFreq) && (ps.extras.getInt("synctype") == 1)) {
						foundSameSyncCycle = true;
					}
				}

				if (!foundSameSyncCycle) {
					Bundle b = new Bundle();
					b.putInt("synctype", 1);

					ContentResolver.removePeriodicSync(myAccountList[i],
                            GeneralSettingsActivity._accountAuthority, b);
                    if (syncFreq > 0) {
                        ContentResolver.addPeriodicSync(myAccountList[i],
                                GeneralSettingsActivity._accountAuthority, b, syncFreq * 60);
                    }
				}

                prefs.putLong(key, syncFreq);
			}
		}
        else if ("sync_bulk_messages".equals(key)) {
            prefs.putInteger(key, Integer.parseInt(value));
        }
	}
}
