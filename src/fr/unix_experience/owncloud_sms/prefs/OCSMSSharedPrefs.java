package fr.unix_experience.owncloud_sms.prefs;

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
