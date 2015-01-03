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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

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
	}
	
	public Integer getServerAPIVersion() throws OCSyncException {
		GetMethod get = createGetVersionRequest();
		JSONObject obj = doHttpRequest(get, true);
		if (obj == null) {
			// Return default version
			return 1;
		}
		
		try {
			  _serverAPIVersion = obj.getInt("version");
		}
		catch (JSONException e) {
			Log.e(TAG, "No version received from server, assuming version 1", e);
			_serverAPIVersion = 1;
		}
		
		return _serverAPIVersion;
	}
	
	public void doPushRequest(JSONArray smsList) throws OCSyncException {
		/**
		 * If we need other API push, set it here
		 */
		switch (_serverAPIVersion) {
			case 2: doPushRequestV2(smsList); break;
			case 1: 
			default: doPushRequestV1(smsList); break;
		}
	}
		
	public void doPushRequestV1(JSONArray smsList) throws OCSyncException {
		// We need to save this date as a step for connectivity change
		Long lastMsgDate = (long) 0;
		
		if (smsList == null) {
			GetMethod get = createGetSmsIdListRequest();
			JSONObject smsGetObj = doHttpRequest(get);
			if (smsGetObj == null) {
				return;
			}
			
			JSONObject smsBoxes = new JSONObject();
			JSONArray inboxSmsList = null, sentSmsList = null, draftsSmsList = null;
			try {
				smsBoxes = smsGetObj.getJSONObject("smslist");
			} catch (JSONException e) {
				try {
					smsGetObj.getJSONArray("smslist");
				} catch (JSONException e2) {
					Log.e(TAG, "Invalid datas received from server (doPushRequest, get SMS list)", e);
					throw new OCSyncException(R.string.err_sync_get_smslist, OCSyncErrorType.PARSE);
				}
			}
			
			try {
				inboxSmsList = smsBoxes.getJSONArray("inbox");
			} catch (JSONException e) {
				Log.d(TAG, "No inbox Sms received from server (doPushRequest, get SMS list)");
			}
			
			try {
				sentSmsList = smsBoxes.getJSONArray("sent");
			} catch (JSONException e) {
				Log.d(TAG, "No sent Sms received from server (doPushRequest, get SMS list)");
			}
			
			try {
				draftsSmsList = smsBoxes.getJSONArray("drafts");
			} catch (JSONException e) {
				Log.d(TAG, "No drafts Sms received from server (doPushRequest, get SMS list)");
			}
			
			SmsFetcher fetcher = new SmsFetcher(_context);
			fetcher.setExistingInboxMessages(inboxSmsList);
			fetcher.setExistingSentMessages(sentSmsList);
			fetcher.setExistingDraftsMessages(draftsSmsList);
			
			smsList = fetcher.fetchAllMessages();
			
			// Get maximum message date present in smsList to keep a step when connectivity changes
			lastMsgDate = fetcher.getLastMessageDate();
		}
		
		if (smsList.length() == 0) {
			Log.d(TAG, "No new SMS to sync, sync done");
			return;
		}
		
		PostMethod post = createPushRequest(smsList);
		if (post == null) {
			Log.e(TAG,"Push request for POST is null");
			throw new OCSyncException(R.string.err_sync_craft_http_request, OCSyncErrorType.IO);
		}
		
		JSONObject obj = doHttpRequest(post);
		if (obj == null) {
			Log.e(TAG,"Request failed. It doesn't return a valid JSON Object");
			throw new OCSyncException(R.string.err_sync_push_request, OCSyncErrorType.IO);
		}
		
		Boolean pushStatus;
		String pushMessage;
		try {
			 pushStatus = obj.getBoolean("status");
			 pushMessage = obj.getString("msg");
		}
		catch (JSONException e) {
			Log.e(TAG, "Invalid datas received from server", e);
			throw new OCSyncException(R.string.err_sync_push_request_resp, OCSyncErrorType.PARSE);
		}

		// Push was OK, we can save the lastMessageDate which was saved to server
		(new OCSMSSharedPrefs(_context)).setLastMessageDate(lastMsgDate);

		Log.d(TAG, "SMS Push request said: status " + pushStatus + " - " + pushMessage);
	}
	
	public void doPushRequestV2(JSONArray smsList) throws OCSyncException {
	
	}
	
	public GetMethod createGetVersionRequest() {
		return createGetRequest(OC_GET_VERSION);
	}
	
	public GetMethod createGetSmsIdListRequest() {
		return createGetRequest(OC_GET_ALL_SMS_IDS);
	}

	public GetMethod createGetSmsIdListWithStateRequest() {
		return createGetRequest(OC_GET_ALL_SMS_IDS_WITH_STATUS);
	}
	
	public GetMethod createGetLastSmsTimestampRequest() {
		return createGetRequest(OC_GET_LAST_MSG_TIMESTAMP);
	}
	
	private GetMethod createGetRequest(String oc_call) {
		GetMethod get = new GetMethod(_ocClient.getBaseUri() + oc_call);
		get.addRequestHeader("OCS-APIREQUEST", "true");
		return get;
	}
	
	public PostMethod createPushRequest() throws OCSyncException {
		SmsFetcher fetcher = new SmsFetcher(_context);
		JSONArray smsList = fetcher.fetchAllMessages();
		return createPushRequest(smsList);
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
		
		PostMethod post = new PostMethod(_ocClient.getBaseUri() + OC_PUSH_ROUTE);
		post.addRequestHeader("OCS-APIREQUEST", "true");
		post.setRequestEntity(ent);
		
		return post;
	}
	
	private JSONObject createPushJSONObject(JSONArray smsList) throws OCSyncException {
		if (smsList == null) {
			Log.e(TAG,"NULL SMS List");
			throw new OCSyncException(R.string.err_sync_create_json_null_smslist, OCSyncErrorType.IO);
		}
		
		JSONObject reqJSON = new JSONObject();

		try {
			reqJSON.put("smsDatas", smsList);
			reqJSON.put("smsCount", smsList == null ? 0 : smsList.length());
		} catch (JSONException e) {
			Log.e(TAG,"JSON Exception when creating JSON request object");
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
			Log.e(TAG,"Unsupported encoding when generating request");
			throw new OCSyncException(R.string.err_sync_create_json_request_encoding, OCSyncErrorType.PARSE);
		}
		
		return requestEntity;
	}
	
	private JSONObject doHttpRequest(HttpMethod req) throws OCSyncException {
		return doHttpRequest(req, false);
	}
	
	// skipError permit to skip invalid JSON datas
	private JSONObject doHttpRequest(HttpMethod req, Boolean skipError) throws OCSyncException {
		JSONObject respJSON = null;
		int status = 0;
		
		// We try maximumHttpReqTries because sometimes network is slow or unstable
		int tryNb = 0;
		ConnectivityMonitor cMon = new ConnectivityMonitor(_context);
		
		while (tryNb < maximumHttpReqTries) {
			tryNb++;
			
			if (!cMon.isValid()) {
				if (tryNb == maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_no_connection_available, OCSyncErrorType.IO);
				}
				continue;
			}
			
			try {
				status = _ocClient.executeMethod(req);
				
				Log.d(TAG, "HTTP Request done at try " + tryNb);
				
				// Force loop exit
				tryNb = maximumHttpReqTries;
			} catch (ConnectException e) {
				Log.e(TAG, "Unable to perform a connection to ownCloud instance", e);
				
				// If it's the last try
				if (tryNb == maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_connect, OCSyncErrorType.IO);
				}
			} catch (HttpException e) {
				Log.e(TAG, "Unable to perform a connection to ownCloud instance", e);
				
				// If it's the last try
				if (tryNb == maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_httpexception, OCSyncErrorType.IO);
				}
			} catch (IOException e) {
				Log.e(TAG, "Unable to perform a connection to ownCloud instance", e);
				
				// If it's the last try
				if (tryNb == maximumHttpReqTries) {
					req.releaseConnection();
					throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
				}
			}
		}
			
		if(status == HttpStatus.SC_OK) {
			String response = null;
			try {
				response = req.getResponseBodyAsString();
			} catch (IOException e) {
				Log.e(TAG, "Unable to parse server response", e);
				throw new OCSyncException(R.string.err_sync_http_request_resp, OCSyncErrorType.IO);
			}
			//Log.d(TAG, "Successful response: " + response);

			// Parse the response
			try {
				respJSON = new JSONObject(response);
			} catch (JSONException e) {
				if (skipError == false) {
					Log.e(TAG, "Unable to parse server response", e);
					throw new OCSyncException(R.string.err_sync_http_request_parse_resp, OCSyncErrorType.PARSE);
				}
				return null;
			}
			 
		} else if (status == HttpStatus.SC_FORBIDDEN) {
			// Authentication failed
			throw new OCSyncException(R.string.err_sync_auth_failed, OCSyncErrorType.AUTH);
		} else {
			// Unk error
			String response = null;
			try {
				response = req.getResponseBodyAsString();
			} catch (IOException e) {
				Log.e(TAG, "Unable to parse server response", e);
				throw new OCSyncException(R.string.err_sync_http_request_resp, OCSyncErrorType.PARSE);
			}
			
			Log.e(TAG, "Server set unhandled HTTP return code " + status);
			if (response != null) {
				Log.e(TAG, "Status code: " + status + ". Response message: " + response);
			} else {
				Log.e(TAG, "Status code: " + status);
			}
			throw new OCSyncException(R.string.err_sync_http_request_returncode_unhandled, OCSyncErrorType.SERVER_ERROR);
		}
		return respJSON;
	}
	
	public OwnCloudClient getOCClient() { return _ocClient; }
	
	private static int maximumHttpReqTries = 3;

	private OwnCloudClient _ocClient;
	private Context _context;
	
	private Integer _serverAPIVersion;
	
	private static String OC_GET_VERSION = "/index.php/apps/ocsms/get/apiversion?format=json";
	private static String OC_GET_ALL_SMS_IDS = "/index.php/apps/ocsms/get/smsidlist?format=json";
	private static String OC_GET_ALL_SMS_IDS_WITH_STATUS = "/index.php/apps/ocsms/get/smsidstate?format=json";
	private static String OC_GET_LAST_MSG_TIMESTAMP = "/index.php/apps/ocsms/get/lastmsgtime?format=json";
	private static String OC_PUSH_ROUTE = "/index.php/apps/ocsms/push?format=json";
	
	private static final String TAG = OCSMSOwnCloudClient.class.getSimpleName();

	
}
