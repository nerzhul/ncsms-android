package fr.unix_experience.owncloud_sms.activities.virtual;

/**
 * Copyright (c) 2013-2015, Loic Blot <loic.blot@unix-experience.fr>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Vector;

public class VirtualSettingsActivity extends PreferenceActivity {
    private static String TAG = VirtualSettingsActivity.class.getSimpleName();
    protected static Context _context;
	protected static int _prefsRessourceFile;
	protected static Vector<BindObjectPref> _boolPrefs = new Vector<>();
	protected static Vector<BindObjectPref> _stringPrefs = new Vector<>();

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
        VirtualSettingsActivity._context = getBaseContext();
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!VirtualSettingsActivity.isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		addPreferencesFromResource(VirtualSettingsActivity._prefsRessourceFile);
		bindPreferences();
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return VirtualSettingsActivity.isXLargeTablet(this) && !VirtualSettingsActivity.isSimplePreferences(this);
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
	 * true if this is forced via the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	protected static boolean isSimplePreferences(Context context) {
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                || !VirtualSettingsActivity.isXLargeTablet(context);
	}

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #bindPreferenceBooleanToValue
	 */
	public void bindPreferenceBooleanToValue(Preference preference, Boolean defValue) {
		// Set the listener to watch for value changes.
		preference
		.setOnPreferenceChangeListener(_bindPreferenceListener);

		// Trigger the listener immediately with the preference's
		// current value.
        _bindPreferenceListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getBoolean(
                        preference.getKey(),
                        defValue
                )
        );
	}

	public void bindPreferenceStringToValue(Preference preference, String defValue) {
		// Set the listener to watch for value changes.
		preference
		.setOnPreferenceChangeListener(_bindPreferenceListener);

		// Trigger the listener immediately with the preference's
		// current value.
        _bindPreferenceListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(
                        preference.getKey(),
                        defValue
                )
        );
	}

	/**
	 * This fragment shows data and sync preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public class DataSyncPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(VirtualSettingsActivity._prefsRessourceFile);

            bindPreferences();
		}
	}

	private void bindPreferences() {
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		for (BindObjectPref pref: VirtualSettingsActivity._stringPrefs) {
            bindPreferenceStringToValue(findPreference(pref.name),
                    (String) pref.value);
        }

        for (BindObjectPref pref: VirtualSettingsActivity._boolPrefs) {
            bindPreferenceBooleanToValue(findPreference(pref.name),
                    (Boolean) pref.value);
        }
	}

	// The preference object, it's only a key value pair
	protected class BindObjectPref {
		public String name;
		public Object value;
		public BindObjectPref(String prefName, Object prefVal) {
			name = prefName;
			value = prefVal;
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private final Preference.OnPreferenceChangeListener _bindPreferenceListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                Log.d(TAG, "Changed list preference " + preference.toString() + " value " + value.toString());
                handleListPreference(preference.getKey(), value.toString(), (ListPreference) preference);

			} else if (preference instanceof CheckBoxPreference) {
                Log.d(TAG, "Changed checkbox preference " + preference.toString() + " value " + value.toString());
                handleCheckboxPreference(preference.getKey(), (Boolean) value);
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				//preference.setSummary(boolValue);
			}
			return true;
		}
	};

    protected void handleCheckboxPreference(String key, Boolean value) {}
    protected void handleListPreference(String key, String value,
                                               ListPreference preference) {}
}
