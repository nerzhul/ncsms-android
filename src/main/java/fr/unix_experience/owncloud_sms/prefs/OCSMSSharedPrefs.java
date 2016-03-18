package fr.unix_experience.owncloud_sms.prefs;

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

import android.content.Context;
import android.content.SharedPreferences;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.defines.DefaultPrefs;

public class OCSMSSharedPrefs extends SharedPrefs {

	public OCSMSSharedPrefs(Context context) {
		super(context, R.string.shared_preference_file);
	}

	public void setLastMessageDate(Long msgDate) {
		SharedPreferences.Editor editor = _sPrefs.edit();
		editor.putLong(_context.getString(R.string.pref_lastmsgdate), msgDate);
		editor.apply();
	}

	public Long getLastMessageDate() {
		return _sPrefs.getLong(_context.getString(R.string.pref_lastmsgdate), 0);
	}

	public Boolean pushOnReceive() {
		return _sPrefs.getBoolean("push_on_receive", DefaultPrefs.pushOnReceive);
	}

	public Boolean syncInWifi() {
		return _sPrefs.getBoolean("sync_wifi", DefaultPrefs.syncWifi);
	}

	public Boolean syncIn2G() {
		return _sPrefs.getBoolean("sync_2g", DefaultPrefs.sync2G);
	}

	public Boolean syncInGPRS() {
		return _sPrefs.getBoolean("sync_gprs", DefaultPrefs.syncGPRS);
	}

	public Boolean syncIn3G() {
		return _sPrefs.getBoolean("sync_3g", DefaultPrefs.sync3G);
	}

	public Boolean syncIn4G() {
		return _sPrefs.getBoolean("sync_4g", DefaultPrefs.sync4G);
	}

	public Boolean syncInOtherModes() {
		return _sPrefs.getBoolean("sync_others", DefaultPrefs.syncOthers);
	}

    public Integer getSyncBulkLimit() {
        return _sPrefs.getInt("sync_bulk_messages", -1);
    }
}
