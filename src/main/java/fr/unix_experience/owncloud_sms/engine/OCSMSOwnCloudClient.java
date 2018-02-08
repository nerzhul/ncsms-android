package fr.unix_experience.owncloud_sms.engine;

/*
 *  Copyright (c) 2014-2016, Loic Blot <loic.blot@unix-experience.fr>
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
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;
import ncsmsgo.SmsBuffer;
import ncsmsgo.SmsPushResponse;

@SuppressWarnings("deprecation")
public class OCSMSOwnCloudClient {

	private static final Integer SERVER_RECOVERY_MSG_LIMIT = 500;

	public OCSMSOwnCloudClient(Context context, Account account) {
		_context = context;
		_serverAPIVersion = 1;

		AccountManager accountManager = AccountManager.get(context);
		String ocURI = accountManager.getUserData(account, "ocURI");
		if (ocURI == null) {
			throw new IllegalStateException(context.getString(R.string.err_sync_account_unparsable));
		}

		try {
			URL serverURL = new URL(ocURI);
			_http = new OCHttpClient(context,
					serverURL, accountManager.getUserData(account, "ocLogin"),
					accountManager.getPassword(account));

			_connectivityMonitor = new ConnectivityMonitor(_context);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(context.getString(R.string.err_sync_account_unparsable));
		}
	}

	public Integer getServerAPIVersion() throws OCSyncException {
		Pair<Integer, Integer> vPair = _http.getVersion();
		_serverAPIVersion = vPair.second;
		if (vPair.first == 200 && _serverAPIVersion > 0) {
			return _serverAPIVersion;
		}

		return 0;
	}

	JSONArray getServerPhoneNumbers() throws OCSyncException {
		Pair<Integer, JSONObject> response = _http.getPhoneList();
		if (response.second == null) {
			return null;
		}

		try {
			return response.second.getJSONArray("phoneList");
		} catch (JSONException e) {
			Log.e(OCSMSOwnCloudClient.TAG, "No phonelist received from server, empty it", e);
			return null;
		}
	}

	public void doPushRequest(SmsBuffer smsBuffer) throws OCSyncException {
		/*
		 * If we need other API push, set it here
		 */
		switch (_serverAPIVersion) {
		case 1:
		default: doPushRequestV1(smsBuffer); break;
		}
	}

	private void doPushRequestV1(SmsBuffer smsBuffer) throws OCSyncException {
		if (smsBuffer == null) {
			Pair<Integer, JSONObject> response = _http.getAllSmsIds();
			if (response.second == null) {
				return;
			}

			// Create new SmsBuffer to get results
			smsBuffer = new SmsBuffer();
		}

		if (smsBuffer.empty()) {
			Log.i(OCSMSOwnCloudClient.TAG, "No new SMS to sync, sync done");
			return;
		}

		Pair<Integer, SmsPushResponse> response = _http.pushSms(smsBuffer);

		if (response.second == null) {
			Log.e(OCSMSOwnCloudClient.TAG,"Push request failed. GoLang response is empty.");
			throw new OCSyncException(R.string.err_sync_push_request, OCSyncErrorType.IO);
		}

		// Push was OK, we can save the lastMessageDate which was saved to server
		(new OCSMSSharedPrefs(_context)).setLastMessageDate(smsBuffer.getLastMessageDate());

		Log.i(OCSMSOwnCloudClient.TAG, "SMS Push request said: status " +
				response.second.getStatus() + " - " + response.second.getMessage());
		Log.i(OCSMSOwnCloudClient.TAG, "LastMessageDate set to: " + smsBuffer.getLastMessageDate());
	}

	JSONObject retrieveSomeMessages(Long start, Integer limit) {
		// This is not allowed by server
		if (limit > OCSMSOwnCloudClient.SERVER_RECOVERY_MSG_LIMIT) {
			Log.e(OCSMSOwnCloudClient.TAG, "Message recovery limit exceeded");
			return null;
		}

		Pair<Integer, JSONObject> response;
		try {
			response = _http.getMessages(start, limit);
		} catch (OCSyncException e) {
			Log.e(OCSMSOwnCloudClient.TAG, "Request failed.");
			return null;
		}

		if ((response.second == null) || !response.second.has("messages")
				|| !response.second.has("last_id")) {
			Log.e(OCSMSOwnCloudClient.TAG,
					"Invalid response received from server, either messages or last_id field is missing.");
			return null;
		}

		return response.second;
	}

	private final OCHttpClient _http;
	private final Context _context;
    private final ConnectivityMonitor _connectivityMonitor;

	private Integer _serverAPIVersion;

	private static final String TAG = OCSMSOwnCloudClient.class.getSimpleName();


}
