package fr.unix_experience.owncloud_sms.broadcast_receivers;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import fr.unix_experience.owncloud_sms.observers.SmsObserver;

public class IncomingSms extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (IncomingSms._mboxObserver == null) {
			Log.d(IncomingSms.TAG,"_mboxObserver == null");
            IncomingSms._mboxObserver = new SmsObserver(new Handler(), context);
			context.getContentResolver().
	    		registerContentObserver(Uri.parse("content://sms"), true, IncomingSms._mboxObserver);
		}
	}
	
	private static SmsObserver _mboxObserver;

	private static final String TAG = IncomingSms.class.getSimpleName();
}
