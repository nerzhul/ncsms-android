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

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;

public class HTTPRequestBuilder {

	private static final String TAG = HTTPRequestBuilder.class.getCanonicalName();
	private final Uri _serverURI;
	private final String _username;
	private final String _password;

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

	public HTTPRequestBuilder(Uri serverURI, String accountName, String accountPassword) {
		_serverURI = serverURI;
		_username = accountName;
		_password = accountPassword;
	}

	private GetMethod get(String oc_call) {
		Log.i(HTTPRequestBuilder.TAG, "Create GET " + _serverURI + oc_call);
		return new GetMethod(_serverURI.toString() + oc_call);
	}

	GetMethod getAllSmsIds() {
		return get(HTTPRequestBuilder.OC_GET_ALL_SMS_IDS);
	}

	public GetMethod getVersion() {
		return get(HTTPRequestBuilder.OC_GET_VERSION);
	}

	PostMethod pushSms(StringRequestEntity ent) {
		PostMethod post = new PostMethod(_serverURI.toString() + HTTPRequestBuilder.OC_PUSH_ROUTE);
		post.setRequestEntity(ent);
		return post;
	}

	GetMethod getPhoneList() {
		return get(HTTPRequestBuilder.OC_V2_GET_PHONELIST);
	}

	GetMethod getMessages(Long start, Integer limit) {
		return get(HTTPRequestBuilder.OC_V2_GET_MESSAGES.
				replace("[START]", start.toString()).replace("[LIMIT]", limit.toString()));
	}

	public int execute(HttpMethod req) throws IOException {
		HttpClient http = new HttpClient();
		String basicAuth = "Basic " +
				Base64.encodeToString((_username + ":" + _password).getBytes(), Base64.NO_WRAP);
		//req.setFollowRedirects(true); // App is SIGKILLED by android when doing this... WTF
		req.setDoAuthentication(true);
		req.addRequestHeader("Authorization", basicAuth);
		return http.executeMethod(req);
	}
}
