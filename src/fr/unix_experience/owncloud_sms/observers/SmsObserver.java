package fr.unix_experience.owncloud_sms.observers;

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

import org.json.JSONArray;

import fr.unix_experience.owncloud_sms.engine.ASyncTask;
import fr.unix_experience.owncloud_sms.engine.ConnectivityMonitor;
import fr.unix_experience.owncloud_sms.engine.OCSMSOwnCloudClient;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.enums.MailboxID;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class SmsObserver extends ContentObserver implements ASyncTask {

	public SmsObserver(Handler handler) {
		super(handler);
	}
	
	public SmsObserver(Handler handler, Context ct) {
		super(handler);
		_context = ct;
	}
	
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.d(TAG, "onChange SmsObserver");
	
		SmsFetcher fetcher = new SmsFetcher(_context);
		JSONArray smsList = fetcher.getLastMessage(MailboxID.ALL);
		
		ConnectivityMonitor cMon = new ConnectivityMonitor(_context);
		
		// Synchronize if network is valid and there are SMS
		if (cMon.isValid() && smsList != null) {
			new SyncTask(_context, smsList).execute();
		}
	}

	public void setContext(Context context) {
		_context = context;
	}
	
	private Context _context;
	
	private static final String TAG = OCSMSOwnCloudClient.class.getSimpleName();
}
