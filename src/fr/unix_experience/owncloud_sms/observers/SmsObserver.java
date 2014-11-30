package fr.unix_experience.owncloud_sms.observers;

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

import org.json.JSONArray;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.engine.OCSMSOwnCloudClient;
import fr.unix_experience.owncloud_sms.engine.SmsFetcher;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class SmsObserver extends ContentObserver {

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
		
		if (_accountMgr == null && _context != null) {
			_accountMgr = AccountManager.get(_context);
		}
		String smsURI = "content://sms";
		
		SmsFetcher sFetch = new SmsFetcher(_context);
		JSONArray smsList = sFetch.getLastMessage(smsURI);
		
		if (smsList != null) {
			new SyncTask(smsList).execute();
		}
	}
	
	private class SyncTask extends AsyncTask<Void, Void, Void>{
		public SyncTask(JSONArray smsList) {
			_smsList = smsList;
		}
		@Override
		protected Void doInBackground(Void... params) {
			// Get ownCloud SMS account list
			Account[] myAccountList = _accountMgr.getAccountsByType(_context.getString(R.string.account_type));
			for (int i = 0; i < myAccountList.length; i++) {
				Log.d(TAG, "int i = 0; i < myAccountList.length; i++" + myAccountList[i] + " SmsObserver");
				Uri serverURI = Uri.parse(_accountMgr.getUserData(myAccountList[i], "ocURI"));
				
				OCSMSOwnCloudClient _client = new OCSMSOwnCloudClient(_context,
					serverURI, _accountMgr.getUserData(myAccountList[i], "ocLogin"),
					_accountMgr.getPassword(myAccountList[i]));
				
				try {
					_client.doPushRequest(_smsList);
				} catch (OCSyncException e) {
					Log.e(TAG, _context.getString(e.getErrorId()));
				}
			}
			return null;
		}
		private JSONArray _smsList;
	}

	public void setContext(Context context) {
		_context = context;
	}
	
	private Context _context;
	private static AccountManager _accountMgr;
	
	private static final String TAG = OCSMSOwnCloudClient.class.getSimpleName();
}
