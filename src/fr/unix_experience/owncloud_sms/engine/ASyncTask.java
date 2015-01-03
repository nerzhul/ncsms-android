package fr.unix_experience.owncloud_sms.engine;

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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;

public interface ASyncTask {
	class SyncTask extends AsyncTask<Void, Void, Void>{
		public SyncTask(Context context, JSONArray smsList) {
			_context = context;
			_smsList = smsList;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Get ownCloud SMS account list
			AccountManager _accountMgr = AccountManager.get(_context);
			
			Account[] myAccountList = _accountMgr.getAccountsByType(_context.getString(R.string.account_type));
			for (int i = 0; i < myAccountList.length; i++) {
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
		
		private Context _context;
		private JSONArray _smsList;
	}
	
	static final String TAG = ASyncTask.class.getSimpleName();
}
