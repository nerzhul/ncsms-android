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

import android.content.Context;
import fr.nrz.androidlib.notifications.NrzNotification;
import fr.unix_experience.owncloud_sms.R;
import fr.unix_experience.owncloud_sms.enums.OCSMSNotificationType;

public class OCSMSNotificationManager {

	public OCSMSNotificationManager(Context context) {
		_context = context;
		_notification = new NrzNotification(_context, R.drawable.ic_stat_ocsms);
	}

	public void setSyncProcessMsg() {
		createNotificationIfPossible(OCSMSNotificationType.SYNC,
				_context.getString(R.string.sync_title),
				_context.getString(R.string.sync_inprogress)
				);
	}

	public void dropSyncProcessMsg() {
		_notification.cancelNotify(OCSMSNotificationType.SYNC.ordinal());
	}

	public void setSyncErrorMsg(String errMsg) {
		createNotificationIfPossible(OCSMSNotificationType.SYNC_FAILED,
				_context.getString(R.string.sync_title),
				_context.getString(R.string.fatal_error) + "\n" + errMsg
				);
	}

	public void dropSyncErrorMsg() {
		_notification.cancelNotify(OCSMSNotificationType.SYNC_FAILED.ordinal());
	}

	public void setDebugMsg(String errMsg) {
		createNotificationIfPossible(OCSMSNotificationType.DEBUG,
				"DEBUG", errMsg
				);
	}

	private void createNotificationIfPossible(OCSMSNotificationType nType, String nTitle, String nMsg) {
		_notification.createNotify(nType.ordinal(), nTitle, nMsg);
	}

	private final Context _context;
	private final NrzNotification _notification;

}
