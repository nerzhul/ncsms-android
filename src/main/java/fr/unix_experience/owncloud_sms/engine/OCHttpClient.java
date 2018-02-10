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
import android.util.Pair;

import java.net.URL;

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSyncErrorType;
import fr.unix_experience.owncloud_sms.exceptions.OCSyncException;
import fr.unix_experience.owncloud_sms.providers.AndroidVersionProvider;
import ncsmsgo.SmsBuffer;
import ncsmsgo.SmsHTTPClient;
import ncsmsgo.SmsIDListResponse;
import ncsmsgo.SmsMessagesResponse;
import ncsmsgo.SmsPhoneListResponse;
import ncsmsgo.SmsPushResponse;

public class OCHttpClient {
	private SmsHTTPClient _smsHttpClient;

	public OCHttpClient(Context context, URL serverURL, String accountName, String accountPassword) {
		_smsHttpClient = new SmsHTTPClient();
		// @TODO: at a point add a flag to permit insecure connections somewhere instead of trusting them
		_smsHttpClient.init(serverURL.toString(), new AndroidVersionProvider(context).getVersionCode(),
				accountName, accountPassword, false);
	}

	private void handleEarlyHTTPStatus(int httpStatus) throws OCSyncException {
		switch (httpStatus) {
			case 403: {
				// Authentication failed
				throw new OCSyncException(R.string.err_sync_auth_failed, OCSyncErrorType.AUTH);
			}
		}
	}

	Pair<Integer, SmsIDListResponse> getAllSmsIds() throws OCSyncException {
		SmsIDListResponse silr = _smsHttpClient.doGetSmsIDList();
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();
		handleEarlyHTTPStatus(httpStatus);
		return new Pair<>(httpStatus, silr);
	}

	// Perform the GoLang doVersionCall and handle return
	public Pair<Integer, Integer> getVersion() throws OCSyncException {
		Integer serverAPIVersion = (int) _smsHttpClient.doVersionCall();
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();

		handleEarlyHTTPStatus(httpStatus);

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
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();
		handleEarlyHTTPStatus(httpStatus);
		return new Pair<>(httpStatus, spr);
	}

	Pair<Integer, SmsPhoneListResponse> getPhoneList() throws OCSyncException {
		SmsPhoneListResponse splr = _smsHttpClient.doGetPhoneList();
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();
		handleEarlyHTTPStatus(httpStatus);
		return new Pair<>(httpStatus, splr);
	}

	Pair<Integer, SmsMessagesResponse> getMessages(Long start, Integer limit) throws OCSyncException {
		SmsMessagesResponse smr = _smsHttpClient.doGetMessagesCall(start, limit);
		int httpStatus = (int) _smsHttpClient.getLastHTTPStatus();
		handleEarlyHTTPStatus(httpStatus);
		return new Pair<>(httpStatus, smr);
	}
}
