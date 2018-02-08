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
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.providers.AndroidVersionProvider;
import ncsmsgo.SmsBuffer;
import ncsmsgo.SmsHTTPClient;
import ncsmsgo.SmsPushResponse;

public class OCHttpClient {
	private SmsHTTPClient _smsHttpClient;

	private static final String TAG = OCHttpClient.class.getCanonicalName();
	private static final String PARAM_PROTOCOL_VERSION = "http.protocol.version";
	private final URL _url;
	private final String _userAgent;
	private final String _username;
	private final String _password;

	public OCHttpClient(Context context, URL serverURL, String accountName, String accountPassword) {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception ignored) {
		}

		_url = serverURL;
		_username = accountName;
		_password = accountPassword;

		_smsHttpClient = new SmsHTTPClient();
		// @TODO: at a point add a flag to permit insecure connections somewhere instead of trusting them
		_smsHttpClient.init(_url.toString(), new AndroidVersionProvider(context).getVersionCode(),
				_username, _password, false);

		_userAgent = "nextcloud-phonesync (" + new AndroidVersionProvider(context).getVersionCode() + ")";
	}

	private Pair<Integer, JSONObject> get(String oc_call, boolean skipError) throws OCSyncException {
		Log.i(OCHttpClient.TAG, "Perform GET " + _url + oc_call);
		try {
			return execute("GET",
					new URL(_url.toString() + oc_call), "", skipError);
		} catch (MalformedURLException e) {
			Log.e(OCHttpClient.TAG, "Malformed URL provided, aborting. URL was: "
					+ _url.toExternalForm() + oc_call);
		}

		return new Pair<>(0, null);
	}

	private Pair<Integer, JSONObject> post(String oc_call, String data) throws OCSyncException {
		Log.i(OCHttpClient.TAG, "Perform POST " + _url + oc_call);
		try {
			return execute("POST",
					new URL(_url.toString() + oc_call), data, false);
		} catch (MalformedURLException e) {
			Log.e(OCHttpClient.TAG, "Malformed URL provided, aborting. URL was: "
					+ _url.toExternalForm() + oc_call);
		}

		return new Pair<>(0, null);
	}

	Pair<Integer, JSONObject> getAllSmsIds() throws OCSyncException {
		return get(_smsHttpClient.getAllSmsIdsCall(), false);
	}

	// Perform the GoLang doVersionCall and handle return
	public Pair<Integer, Integer> getVersion() throws OCSyncException {
		Integer serverAPIVersion = (int) _smsHttpClient.doVersionCall();
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();

		// If last status is not 200, send the wrong status now
		if (httpStatus != 200) {
			return new Pair<>(httpStatus, 0);
		}

		if (serverAPIVersion > 0) {
			return new Pair<>(200, serverAPIVersion);
		}
		else if (serverAPIVersion == 0) {
			// Return default version
			return new Pair<>(200, 1);
		}
		else if (serverAPIVersion == -1) {
			// This return code from API means I/O error
			throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
		}
		else {
			throw new OCSyncException(R.string.err_sync_http_request_returncode_unhandled, OCSyncErrorType.SERVER_ERROR);
		}
	}

	Pair<Integer, SmsPushResponse> pushSms(SmsBuffer smsBuf) throws OCSyncException {
		SmsPushResponse spr = _smsHttpClient.doPushCall(smsBuf);
		return new Pair<>((int) _smsHttpClient.getLastHTTPStatus(), spr);
	}

	Pair<Integer, JSONObject> getPhoneList() throws OCSyncException {
		return get(_smsHttpClient.getPhoneListCall(), true);
	}

	Pair<Integer, JSONObject> getMessages(Long start, Integer limit) throws OCSyncException {
		return get(_smsHttpClient.getMessagesCall()
				.replace("[START]", start.toString())
				.replace("[LIMIT]", limit.toString()), false);
	}

	public Pair<Integer, JSONObject> execute(String method, URL url, String requestBody, boolean skipError) throws OCSyncException {
		Pair<Integer, JSONObject> response;
		HttpURLConnection urlConnection = null;

		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Log.e(OCHttpClient.TAG, "Failed to open connection to server: " + e);
			throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}

		if (urlConnection == null) {
			Log.e(OCHttpClient.TAG, "Failed to open connection to server: null urlConnection");
			throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
		}

		try {
			urlConnection.setRequestMethod(method);
		} catch (ProtocolException e) {
			Log.e(OCHttpClient.TAG, "Fatal error when setting request method: " + e);
			throw new OCSyncException(R.string.err_sync_http_request_protocol_exception, OCSyncErrorType.IO);
		}
		urlConnection.setRequestProperty("User-Agent", _userAgent);
		urlConnection.setInstanceFollowRedirects(true);
		if (!"GET".equals(method)) {
			urlConnection.setDoOutput(true);
		}
		urlConnection.setRequestProperty("Content-Type", "application/json");
		urlConnection.setRequestProperty("Accept", "application/json");
		if (!"GET".equals(method)) {
			urlConnection.setRequestProperty("Transfer-Encoding", "chunked");
		}
		String basicAuth = "Basic " +
				Base64.encodeToString((_username + ":" + _password).getBytes(), Base64.NO_WRAP);
		urlConnection.setRequestProperty("Authorization", basicAuth);
		urlConnection.setChunkedStreamingMode(0);

		if (!"GET".equals(method)) {
			try {
				OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
				out.write(requestBody.getBytes(Charset.forName("UTF-8")));
				out.close();
			} catch (IOException e) {
				Log.e(OCHttpClient.TAG, "Failed to open connection to server: " + e);
				throw new OCSyncException(R.string.err_sync_http_write_failed, OCSyncErrorType.IO);
			}
		}

		response = handleHTTPResponse(urlConnection, skipError);
		return response;
	}

	private Pair<Integer, JSONObject> handleHTTPResponse(HttpURLConnection connection, Boolean skipError) throws OCSyncException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}

			String response = stringBuilder.toString();
			int status = connection.getResponseCode();

			switch (status) {
				case 200: {
					// Parse the response
					try {
						JSONObject jsonResponse = new JSONObject(response);
						return new Pair<>(status, jsonResponse);

					} catch (JSONException e) {
						if (!skipError) {
							if (response.contains("ownCloud") && response.contains("DOCTYPE")) {
								Log.e(OCHttpClient.TAG, "OcSMS app not enabled or ownCloud upgrade is required");
								throw new OCSyncException(R.string.err_sync_ocsms_not_installed_or_oc_upgrade_required,
										OCSyncErrorType.SERVER_ERROR);
							} else {
								Log.e(OCHttpClient.TAG, "Unable to parse server response", e);
								throw new OCSyncException(R.string.err_sync_http_request_parse_resp, OCSyncErrorType.PARSE);
							}
						}
					}
					break;
				}
				case 403: {
					// Authentication failed
					throw new OCSyncException(R.string.err_sync_auth_failed, OCSyncErrorType.AUTH);
				}
				default: {
					// Unk error
					Log.e(OCHttpClient.TAG, "Server set unhandled HTTP return code " + status);
					Log.e(OCHttpClient.TAG, "Status code: " + status + ". Response message: " + response);
					throw new OCSyncException(R.string.err_sync_http_request_returncode_unhandled, OCSyncErrorType.SERVER_ERROR);
				}
			}

			reader.close();
		}
		catch (IOException e) {
			throw new OCSyncException(R.string.err_sync_http_request_ioexception, OCSyncErrorType.IO);
		}

		return new Pair<>(0, null);
	}
}
