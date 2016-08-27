package fr.unix_experience.owncloud_sms.authenticators;

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

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.IOException;

import fr.unix_experience.owncloud_sms.activities.LoginActivity;
import fr.unix_experience.owncloud_sms.enums.LoginReturnCode;

public class OwnCloudAuthenticator extends AbstractAccountAuthenticator {
    // Simple constructor
    public OwnCloudAuthenticator(Context context) {
        super(context);
        _context = context;
    }

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		Bundle result;
		Intent intent;
		                
		intent = new Intent(_context, LoginActivity.class);  
		
		result = new Bundle();  
		result.putParcelable(AccountManager.KEY_INTENT, intent);  
		return result; 
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * Return codes
	 * 1: invalid address
	 * 2: HTTP failed
	 * 3: connexion failed
	 * 4: invalid login
	 * 5: unknown error
	 */
	public LoginReturnCode testCredentials() {
		LoginReturnCode bRet = LoginReturnCode.OK;
		GetMethod get;
		int status;
		
		try {
		    get = new GetMethod(_client.getBaseUri() + "/index.php/ocs/cloud/user?format=json");
		} catch (IllegalArgumentException e) {
			return LoginReturnCode.INVALID_ADDR;
		}
		
		get.addRequestHeader("OCS-APIREQUEST", "true");
		
		try {
			status = _client.executeMethod(get);
		} catch (IllegalArgumentException e) {
			return LoginReturnCode.INVALID_ADDR;
		} catch (HttpException e) {
			return LoginReturnCode.HTTP_CONN_FAILED;
		} catch (IOException e) {
			return LoginReturnCode.CONN_FAILED;
		}
		
		try {
			if(OwnCloudAuthenticator.isSuccess(status)) {
				 String response = get.getResponseBodyAsString();
				 Log.d(OwnCloudAuthenticator.TAG, "Successful response: " + response);

				 // Parse the response
				 JSONObject respJSON = new JSONObject(response);
				 JSONObject respOCS = respJSON.getJSONObject(OwnCloudAuthenticator.NODE_OCS);
				 JSONObject respData = respOCS.getJSONObject(OwnCloudAuthenticator.NODE_DATA);
				 String id = respData.getString(OwnCloudAuthenticator.NODE_ID);
				 String displayName = respData.getString(OwnCloudAuthenticator.NODE_DISPLAY_NAME);
				 String email = respData.getString(OwnCloudAuthenticator.NODE_EMAIL);
				 
				 Log.d(OwnCloudAuthenticator.TAG, "*** Parsed user information: " + id + " - " + displayName + " - " + email);
				 
			} else {
				String response = get.getResponseBodyAsString();
				Log.e(OwnCloudAuthenticator.TAG, "Failed response while getting user information ");
				if (response != null) {
					Log.e(OwnCloudAuthenticator.TAG, "*** status code: " + status + " ; response message: " + response);
				} else {
					Log.e(OwnCloudAuthenticator.TAG, "*** status code: " + status);
				}

                switch (status) {
                    case 401: bRet = LoginReturnCode.INVALID_LOGIN; break;
                    case 404: bRet = LoginReturnCode.CONN_FAILED_NOT_FOUND; break;
                    default: bRet = LoginReturnCode.UNKNOWN_ERROR; break;
                }
			}
			
		} catch (Exception e) {
			Log.e(OwnCloudAuthenticator.TAG, "Exception while getting OC user information", e);
			bRet = LoginReturnCode.UNKNOWN_ERROR;
			
		} finally {
			get.releaseConnection();
		}
		return bRet;
	}
	
	private static boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }
	
	public void setClient(OwnCloudClient oc) {
		_client = oc;
	}

	private final Context _context;
	private OwnCloudClient _client;
	
	private static final String TAG = OwnCloudAuthenticator.class.getSimpleName();
	
	private static final String NODE_OCS = "ocs";
	private static final String NODE_DATA = "data";
	private static final String NODE_ID = "id";
	private static final String NODE_DISPLAY_NAME= "display-name";
	private static final String NODE_EMAIL= "email";
}