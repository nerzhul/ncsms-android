package fr.unix_experience.owncloud_sms.activities;

/*
 *  Copyright (c) 2014-2017, Loic Blot <loic.blot@unix-experience.fr>
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
import android.content.ContentResolver;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;

import java.util.List;
import java.util.Vector;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.activities.virtual.VirtualSettingsActivity;
import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;
import fr.unix_experience.owncloud_sms.prefs.PermissionChecker;

import static fr.unix_experience.owncloud_sms.enums.PermissionID.REQUEST_ACCOUNTS;

public class OCSMSSettingsActivity extends VirtualSettingsActivity {
	private static final String TAG = OCSMSSettingsActivity.class.getSimpleName();

	private static AccountManager _accountMgr;
	private static String _accountAuthority;
	private static String _accountType;
	private static Vector<Pair<Integer, Boolean>> _boolSettings;

	private AppCompatDelegate mDelegate;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private AppCompatDelegate getDelegate() {
		if (mDelegate == null) {
			mDelegate = AppCompatDelegate.create(this, null);
		}
		return mDelegate;
	}

	public ActionBar getSupportActionBar() {
		return getDelegate().getSupportActionBar();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		OCSMSSettingsActivity._accountMgr = AccountManager.get(getBaseContext());
		OCSMSSettingsActivity._accountAuthority = getString(R.string.account_authority);
		OCSMSSettingsActivity._accountType = getString(R.string.account_type);
		VirtualSettingsActivity._prefsRessourceFile = R.xml.pref_data_sync;

		// Bind our boolean preferences
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_push_on_receive, DefaultPrefs.pushOnReceive));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_show_sync_notifications, DefaultPrefs.showSyncNotifications));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_wifi, DefaultPrefs.syncWifi));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_4g, DefaultPrefs.sync4G));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_3g, DefaultPrefs.sync3G));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_gprs, DefaultPrefs.syncGPRS));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_2g, DefaultPrefs.sync2G));
		VirtualSettingsActivity._boolPrefs.add(
				new BindObjectPref(R.string.setting_sync_others, DefaultPrefs.syncOthers));

		// Bind our string preferences
		VirtualSettingsActivity._stringPrefs.add(
				new BindObjectPref(R.string.setting_sync_frequency, "15"));
		VirtualSettingsActivity._stringPrefs.add(
				new BindObjectPref(R.string.setting_sync_bulk_messages, "-1"));
		VirtualSettingsActivity._stringPrefs.add(
				new BindObjectPref(R.string.setting_minimum_sync_chars, "1"));

		// Must be at the end, after preference bind
		super.onPostCreate(savedInstanceState);
	}

	protected void handleCheckboxPreference(String key, Boolean value) {
		// Network types allowed for sync
		if ("push_on_receive".equals(key) || "show_sync_notifications".equals(key) ||
				"sync_wifi".equals(key) || "sync_2g".equals(key) ||
				"sync_3g".equals(key) || "sync_gprs".equals(key) ||
				"sync_4g".equals(key) || "sync_others".equals(key)) {
			OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(VirtualSettingsActivity._context);
			Log.i(OCSMSSettingsActivity.TAG, "OCSMSSettingsActivity.handleCheckboxPreference: set " + key + " to "
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
		preference.setSummary((index >= 0) ? preference.getEntries()[index] : null);

		Log.i(OCSMSSettingsActivity.TAG, "Modifying listPreference " + key);

		OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(VirtualSettingsActivity._context);

		// Handle sync frequency change
		if ("sync_frequency".equals(key)) {
			if (!PermissionChecker.checkPermission(this, Manifest.permission.GET_ACCOUNTS,
					REQUEST_ACCOUNTS)) {
				return;
			}

			Account[] myAccountList = OCSMSSettingsActivity._accountMgr.getAccountsByType(OCSMSSettingsActivity._accountType);
			long syncFreq = Long.parseLong(value);

			// Get ownCloud SMS account list
			for (Account acct : myAccountList) {
				// And get all authorities for this account
				List<PeriodicSync> syncList = ContentResolver.getPeriodicSyncs(acct, OCSMSSettingsActivity._accountAuthority);

				boolean foundSameSyncCycle = false;
				for (PeriodicSync ps : syncList) {
					if ((ps.period == syncFreq) && (ps.extras.getInt("synctype") == 1)) {
						foundSameSyncCycle = true;
					}
				}

				if (!foundSameSyncCycle) {
					Bundle b = new Bundle();
					b.putInt("synctype", 1);

					ContentResolver.removePeriodicSync(acct, OCSMSSettingsActivity._accountAuthority, b);
					if (syncFreq > 0) {
						ContentResolver.addPeriodicSync(acct, OCSMSSettingsActivity._accountAuthority, b, syncFreq * 60);
					}
				}

				prefs.putLong(key, syncFreq);
			}
		} else if ("sync_bulk_messages".equals(key) || "minimum_sync_chars".equals(key)) {
			prefs.putInteger(key, Integer.parseInt(value));
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);

		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			default:
				return false;
		}
		return true;
	}
}
