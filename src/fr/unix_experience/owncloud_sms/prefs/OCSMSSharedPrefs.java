package fr.unix_experience.owncloud_sms.prefs;

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

import fr.unix_experience.owncloud_sms.R;
import android.content.Context;
import android.content.SharedPreferences;

public class OCSMSSharedPrefs {

	public OCSMSSharedPrefs(Context context) {
		_context = context;
		
		_sPrefs = _context.getSharedPreferences(_context.getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
	}
	
	public void setLastMessageDate(Long msgDate) {
		SharedPreferences.Editor editor = _sPrefs.edit();
		editor.putLong(_context.getString(R.string.pref_lastmsgdate), msgDate);
		editor.commit();
	}
	
	public Long getLastMessageDate() {
		return _sPrefs.getLong(_context.getString(R.string.pref_lastmsgdate), 0);
	}
	
	private SharedPreferences _sPrefs;
	private Context _context;
}
