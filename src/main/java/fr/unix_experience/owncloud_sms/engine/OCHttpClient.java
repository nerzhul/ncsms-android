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

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.IOException;
import java.net.URL;

import fr.unix_experience.owncloud_sms.providers.AndroidVersionProvider;

public class OCHttpClient extends HttpClient {
	static {
		System.loadLibrary("nativesms");
	}

	public static native String getAllSmsIdsCall();
	public static native String getLastMsgTimestamp();
	public static native String getPushRoute();
	public static native String getVersionCall();

	private static final String TAG = OCHttpClient.class.getCanonicalName();
	private static final String PARAM_PROTOCOL_VERSION = "http.protocol.version";
	private final URL _serverURI;
	private final String _username;
	private final String _password;

	// API v2 calls
	private static final String OC_V2_GET_PHONELIST = "/index.php/apps/ocsms/api/v2/phones/list?format=json";
	private static final String OC_V2_GET_MESSAGES ="/index.php/apps/ocsms/api/v2/messages/[START]/[LIMIT]?format=json";
	private static final String OC_V2_GET_MESSAGES_PHONE ="/index.php/apps/ocsms/api/v2/messages/[PHONENUMBER]/[START]/[LIMIT]?format=json";
	private static final String OC_V2_GET_MESSAGES_SENDQUEUE = "/index.php/apps/ocsms/api/v2/messages/sendqueue?format=json";

	public OCHttpClient(Context context, URL serverURL, String accountName, String accountPassword) {
		super(new MultiThreadedHttpConnectionManager());
		Protocol easyhttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", easyhttps);
		_serverURI = serverURL;
		_username = accountName;
		_password = accountPassword;
		getParams().setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
		getParams().setParameter(OCHttpClient.PARAM_PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		getParams().setParameter(HttpMethodParams.USER_AGENT,
				"nextcloud-phonesync (" + new AndroidVersionProvider(context).getVersionCode() + ")");
	}

	private GetMethod get(String oc_call) {
		Log.i(OCHttpClient.TAG, "Create GET " + _serverURI + oc_call);
		return new GetMethod(_serverURI.toString() + oc_call);
	}

	GetMethod getAllSmsIds() {
		return get(OCHttpClient.getAllSmsIdsCall());
	}

	public GetMethod getVersion() {
		return get(OCHttpClient.getVersionCall());
	}

	PostMethod pushSms(StringRequestEntity ent) {
		PostMethod post = new PostMethod(_serverURI.toString() + OCHttpClient.getPushRoute());
		post.setRequestEntity(ent);
		return post;
	}

	GetMethod getPhoneList() {
		return get(OCHttpClient.OC_V2_GET_PHONELIST);
	}

	GetMethod getMessages(Long start, Integer limit) {
		return get(OCHttpClient.OC_V2_GET_MESSAGES.
				replace("[START]", start.toString()).replace("[LIMIT]", limit.toString()));
	}

	private int followRedirections(HttpMethod httpMethod) throws IOException {
		int redirectionsCount = 0;
		int status = httpMethod.getStatusCode();
		while ((redirectionsCount < 3) &&
				((status == HttpStatus.SC_MOVED_PERMANENTLY) ||
						(status == HttpStatus.SC_MOVED_TEMPORARILY) ||
						(status == HttpStatus.SC_TEMPORARY_REDIRECT))
				) {
			Header location = httpMethod.getResponseHeader("Location");
			if (location == null) {
				location = httpMethod.getResponseHeader("location");
			}
			if (location == null) {
				Log.e(OCHttpClient.TAG, "No valid location header found when redirecting.");
				return 500;
			}

			try {
				httpMethod.setURI(new URI(location.getValue()));
			} catch (URIException e) {
				Log.e(OCHttpClient.TAG, "Invalid URI in 302 FOUND response");
				return 500;
			}

			status = executeMethod(httpMethod);
			redirectionsCount++;
		}

		if (((redirectionsCount >= 3) && (status == HttpStatus.SC_MOVED_PERMANENTLY)) ||
				(status == HttpStatus.SC_MOVED_TEMPORARILY) ||
				(status == HttpStatus.SC_TEMPORARY_REDIRECT)) {
			Log.e(OCHttpClient.TAG, "Too many redirection done. Aborting, please ensure your server is " +
					"correctly configured");
			return 400;
		}

		return status;
	}

	public int execute(HttpMethod req) throws IOException {
		String basicAuth = "Basic " +
				Base64.encodeToString((_username + ":" + _password).getBytes(), Base64.NO_WRAP);
		req.setDoAuthentication(true);
		req.addRequestHeader("Authorization", basicAuth);
		executeMethod(req);
		return followRedirections(req);
	}
}
