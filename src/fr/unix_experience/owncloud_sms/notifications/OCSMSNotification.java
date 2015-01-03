package fr.unix_experience.owncloud_sms.notifications;

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

import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class OCSMSNotification {
	public OCSMSNotification(Context ct) {
		_ct = ct;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean createNotify(OCSMSNotificationType nType, String nTitle, String nText) {
		if (_ct == null) {
			return false;
		}
		NotificationManager notificationManager = (NotificationManager)_ct.getSystemService(Context.NOTIFICATION_SERVICE);        
	
		Builder mBuilder = new Notification.Builder(_ct)
				.setContentText(nText)
				.setContentTitle(nTitle)
				.setSmallIcon(R.drawable.ic_launcher);

		notificationManager.notify(nType.ordinal(), mBuilder.build());
	    return true;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void cancelNotify(OCSMSNotificationType nType) {
		NotificationManager notificationManager = (NotificationManager)_ct.getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.cancel(nType.ordinal());
	}
	
	private Context _ct;
}
