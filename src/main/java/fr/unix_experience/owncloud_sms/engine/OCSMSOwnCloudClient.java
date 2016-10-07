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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

@SuppressWarnings("deprecation")
public class OCSMSOwnCloudClient {

	public OCSMSOwnCloudClient(Context context, Uri serverURI, String accountName, String accountPassword) {
		_context = context;

		_ocClient = OwnCloudClientFactory.createOwnCloudClient(
				serverURI, _context, true);

		// Set basic credentials
		_ocClient.setCredentials(
				OwnCloudCredentialsFactory.newBasicCredentials(accountName, accountPassword)
				);

		_serverAPIVersion = 1;

        _connectivityMonitor = new ConnectivityMonitor(_context);
	}

	public Integer getServerAPIVersion() throws OCSyncException {
		GetMethod get = createGetVersionRequest();
		doHttpRequest(get, true);
		if (_jsonQueryBuffer == null) {
			// Return default version
			return 1;
		}

		try {
			_serverAPIVersion = _jsonQueryBuffer.getInt("version");
		}
		catch (JSONException e) {
			Log.e(OCSMSOwnCloudClient.TAG, "No version received from server, assuming version 1", e);
			_serverAPIVersion = 1;
		}

		return _serverAPIVersion;
	}

	public JSONArray getServerPhoneNumbers() throws OCSyncException {
		GetMethod get = createGetPhoneListRequest();
		doHttpRequest(get, true);
		if (_jsonQueryBuffer == null) {
			return null;
		}

		try {
			return _jsonQueryBuffer.getJSONArray("phoneList");
		} catch (JSONException e) {
			Log.e(OCSMSOwnCloudClient.TAG, "No phonelist received from server, empty it", e);
			return null;
		}
	}

	public void doPushRequest(JSONArray smsList) throws OCSyncException {
		/**
		 * If we need other API push, set it here
		 */
		switch (_serverAPIVersion) {
		case 1:
		default: doPushRequestV1(smsList); break;
		}
	}

	public void doPushRequestV1(JSONArray smsList) throws OCSyncException {
		// We need to save this date as a step for connectivity change
		Long lastMsgDate = (long) 0;

		if (smsList == null) {
			GetMethod get = createGetSmsIdListRequest();
			doHttpRequest(get);
			if (_jsonQueryBuffer == null) {
				return;
			}

			JSONObject smsBoxes = new JSONObject();
			JSONArray inboxSmsList = null, sentSmsList = null, draftsSmsList = null;
			try {
				smsBoxes = _jsonQueryBuffer.getJSONObject("smslist");
			} catch (JSONException e) {
				try {
                    _jsonQueryBuffer.getJSONArray("smslist");
				} catch (JSONException e2) {
					Log.e(OCSMSOwnCloudClient.TAG, "Invalid datas received from server (doPushRequest, get SMS list)", e);
					throw new OCSyncException(R.string.err_sync_get_smslist, OCSyncErrorType.PARSE);
				}
			}

			try {
				inboxSmsList = smsBoxes.getJSONArray("inbox");
			} catch (JSONException e) {
				Log.i(OCSMSOwnCloudClient.TAG, "No inbox Sms received from server (doPushRequest, get SMS list)");
			}

			try {
				sentSmsList = smsBoxes.getJSONArray("sent");
			} catch (JSONException e) {
				Log.i(OCSMSOwnCloudClient.TAG, "No sent Sms received from server (doPushRequest, get SMS list)");
			}

			try {
				draftsSmsList = smsBoxes.getJSONArray("drafts");
			} catch (JSONException e) {
				Log.i(OCSMSOwnCloudClient.TAG, "No drafts Sms received from server (doPushRequest, get SMS list)");
			}

			SmsFetcher fetcher = new SmsFetcher(_context);
			fetcher.setExistingInboxMessages(inboxSmsList);
			fetcher.setExistingSentMessages(sentSmsList);
			fetcher.setExistingDraftsMessages(draftsSmsList);

            smsList = new JSONArray();
			fetcher.fetchAllMessages(smsList);

			// Get maximum message date present in smsList to keep a step when connectivity changes
			lastMsgDate = fetcher.getLastMessageDate();
		}

		if (smsList.length() == 0) {
			Log.i(OCSMSOwnCloudClient.TAG, "No new SMS to sync, sync done");
			return;
		}

		PostMethod post = createPushRequest(smsList);
		if (post == null) {
			Log.e(OCSMSOwnCloudClient.TAG,"Push request for POST is null");
			throw new OCSyncException(R.string.err_sync_craft_http_request, OCSyncErrorType.IO);
		}

		doHttpRequest(post);
		if (_jsonQueryBuffer == null) {
			Log.e(OCSMSOwnCloudClient.TAG,"Request failed. It doesn't return a valid JSON Object");
			throw new OCSyncException(R.string.err_sync_push_request, OCSyncErrorType.IO);
		}

		Boolean pushStatus;
		String pushMessage;
		try {
			pushStatus = _jsonQueryBuffer.getBoolean("status");
			pushMessage = _jsonQueryBuffer.getString("msg");
		}
		catch (JSONException e) {
			Log.e(OCSMSOwnCloudClient.TAG, "Invalid datas received from server", e);
			throw new OCSyncException(R.string.err_sync_push_request_resp, OCSyncErrorType.PARSE);
		}

		// Push was OK, we can save the lastMessageDate which was saved to server
		(new OCSMSSharedPrefs(_context)).setLastMessageDate(lastMsgDate);

		Log.i(OCSMSOwnCloudClient.TAG, "SMS Push request said: status " + pushStatus + " - " + pushMessage);
	}

	public GetMethod createGetVersionRequest() {
		return createGetRequest(OCSMSOwnCloudClient.OC_GET_VERSION);
	}

	public GetMethod createGetPhoneListRequest() {
		return createGetRequest(OCSMSOwnCloudClient.OC_V2_GET_PHONELIST);
	}

	public GetMethod createGetSmsIdListRequest() {
		return createGetRequest(OCSMSOwnCloudClient.OC_GET_ALL_SMS_IDS);
	}

	public GetMethod createGetLastSmsTimestampRequest() {
		return createGetRequest(OCSMSOwnCloudClient.OC_GET_LAST_MSG_TIMESTAMP);
	}

	private GetMethod createGetRequest(String oc_call) {
		GetMethod get = new GetMethod(_ocClient.getBaseUri() + oc_call);
		get.addRequestHeader("OCS-APIREQUEST", "true");
		return get;
	}

    public PostMethod createPushRequest(JSONArray smsList) throws OCSyncException {
		JSONObject obj = createPushJSONObject(smsList);
		if (obj == null) {
			return null;
		}

		StringRequestEntity ent = createJSONRequestEntity(obj);
		if (ent == null) {
			return null;
		}

		PostMethod post = new PostMethod(_ocClient.getBaseUri() + OCSMSOwnCloudClient.OC_PUSH_ROUTE);
		post.addRequestHeader("OCS-APIREQUEST", "true");
		post.setRequestEntity(ent);

		return post;
	}

	private JSONObject createPushJSONObject(JSONArray smsList) throws OCSyncException {
		if (smsList == null) {
			Log.e(OCSMSOwnCloudClient.TAG,"NULL SMS List");
			throw new OCSyncException(R.string.err_sync_create_json_null_smslist, OCSyncErrorType.IO);
		}

		JSONObject reqJSON = new JSONObject();

		try {
			reqJSON.put("smsDatas", smsList);
			reqJSON.put("smsCount", smsList.length());
		} catch (JSONException e) {
			Log.e(OCSMSOwnCloudClient.TAG,"JSON Exception when creating JSON request object");
			throw new OCSyncException(R.string.err_sync_create_json_put_smslist, OCSyncErrorType.PARSE);
		}

		return reqJSON;
	}

	private StringRequestEntity createJSONRequestEntity(JSONObject obj) throws OCSyncException {
		StringRequestEntity requestEntity;
		try {
			requestEntity = new StringRequestEntity(
					obj.toString(),
					"application/json",
					"UTF-8");

		} catch (UnsupportedEncodingException e) {
			Log.e(OCSMSOwnCloudClient.TAG,"Unsupported encoding when generating request");
			throw new OCSyncException(R.string.err_sync_create_json_request_encoding, OCSyncErrorType.PARSE);
		}

		return requestEntity;
	}

	private void doHttpRequest(HttpMethod req) throws OCSyncException {
		doHttpRequest(req, false);
	}

	// skipError permit to skip invalid JSON datas
	private void doHttpRequest(HttpMethod req, Boolean skipError) throws OCSyncException {
        // Reinit the queryBuffer
        _jsonQueryBuffer = null;
		int status = 0;
		// We try maximumHttpReqTries because sometimes network is slow or unstable
		int tryNb = 0;

		while (tryNb < OCSMSOwnCloudClient.maximumHttpReqTries) {
			tryNb++;

			if (!_connectivityMonitor.isValid()) {
				if (tryNb == OCSMSOwnCloudClient.maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_no_connection_available, OCSyncErrorType.IO);
				}
				continue;
			}

			try {
				status = _ocClient.executeMethod(req);

				Log.i(OCSMSOwnCloudClient.TAG, "HTTP Request done at try " + tryNb);

				// Force loop exit
				tryNb = OCSMSOwnCloudClient.maximumHttpReqTries;
			} catch (ConnectException e) {
				Log.e(OCSMSOwnCloudClient.TAG, "Unable to perform a connection to ownCloud instance", e);

				// If it's the last try
				if (tryNb == OCSMSOwnCloudClient.maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_connect, OCSyncErrorType.IO);
				}
			} catch (HttpException e) {
				Log.e(OCSMSOwnCloudClient.TAG, "Unable to perform a connection to ownCloud instance", e);

				// If it's the last try
				if (tryNb == OCSMSOwnCloudClient.maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_httpexception, OCSyncErrorType.IO);
				}
			} catch (IOException e) {
				Log.e(OCSMSOwnCloudClient.TAG, "Unable to perform a connection to ownCloud instance", e);

				// If it's the last try
				if (tryNb == OCSMSOwnCloudClient.maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
				}
			}
		}

		if (status == HttpStatus.SC_OK) {
			String response;
			try {
				response = req.getResponseBodyAsString();
			} catch (IOException e) {
				Log.e(OCSMSOwnCloudClient.TAG, "Unable to parse server response", e);
				throw new OCSyncException(R.string.err_sync_http_request_resp, OCSyncErrorType.IO);
			}
			//Log.i(TAG, "Successful response: " + response);

			// Parse the response
			try {
                _jsonQueryBuffer = new JSONObject(response);
			} catch (JSONException e) {
				if (!skipError) {
					if (response.contains("ownCloud") && response.contains("DOCTYPE")) {
						Log.e(OCSMSOwnCloudClient.TAG, "OcSMS app not enabled or ownCloud upgrade is required");
						throw new OCSyncException(R.string.err_sync_ocsms_not_installed_or_oc_upgrade_required,
								OCSyncErrorType.SERVER_ERROR);
					}
					else {
						Log.e(OCSMSOwnCloudClient.TAG, "Unable to parse server response", e);
						throw new OCSyncException(R.string.err_sync_http_request_parse_resp, OCSyncErrorType.PARSE);
					}
				}
                return;
			}

		} else if (status == HttpStatus.SC_FORBIDDEN) {
			// Authentication failed
			throw new OCSyncException(R.string.err_sync_auth_failed, OCSyncErrorType.AUTH);
		} else {
			// Unk error
			String response;
			try {
				response = req.getResponseBodyAsString();
			} catch (IOException e) {
				Log.e(OCSMSOwnCloudClient.TAG, "Unable to parse server response", e);
				throw new OCSyncException(R.string.err_sync_http_request_resp, OCSyncErrorType.PARSE);
			}

			Log.e(OCSMSOwnCloudClient.TAG, "Server set unhandled HTTP return code " + status);
			if (response != null) {
				Log.e(OCSMSOwnCloudClient.TAG, "Status code: " + status + ". Response message: " + response);
			} else {
				Log.e(OCSMSOwnCloudClient.TAG, "Status code: " + status);
			}
			throw new OCSyncException(R.string.err_sync_http_request_returncode_unhandled, OCSyncErrorType.SERVER_ERROR);
		}
	}

    private static final int maximumHttpReqTries = 3;

	private final OwnCloudClient _ocClient;
	private final Context _context;
    private final ConnectivityMonitor _connectivityMonitor;

	private Integer _serverAPIVersion;
    private JSONObject _jsonQueryBuffer;

    // API v1 calls
	private static final String OC_GET_VERSION = "/index.php/apps/ocsms/get/apiversion?format=json";
	private static final String OC_GET_ALL_SMS_IDS = "/index.php/apps/ocsms/get/smsidlist?format=json";
	private static final String OC_GET_LAST_MSG_TIMESTAMP = "/index.php/apps/ocsms/get/lastmsgtime?format=json";
	private static final String OC_PUSH_ROUTE = "/index.php/apps/ocsms/push?format=json";

    // API v2 calls
	private static final String OC_V2_GET_PHONELIST = "/index.php/apps/ocsms/api/v2/phones/list?format=json";
    private static final String OC_V2_GET_MESSAGES ="/index.php/apps/ocsms/api/v2/messages/[START]/[LIMIT]?format=json";
    private static final String OC_V2_GET_MESSAGES_PHONE ="/index.php/apps/ocsms/api/v2/messages/[PHONENUMBER]/[START]/[LIMIT]?format=json";
    private static final String OC_V2_GET_MESSAGES_SENDQUEUE = "/index.php/apps/ocsms/api/v2/messages/sendqueue?format=json";

	private static final String TAG = OCSMSOwnCloudClient.class.getSimpleName();


}
