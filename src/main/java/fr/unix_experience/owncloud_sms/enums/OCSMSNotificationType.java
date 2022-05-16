package fr.unix_experience.owncloud_sms.enums;

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

public enum OCSMSNotificationType {
	SYNC(OCSMSNotificationChannel.SYNC, 0),
	SYNC_FAILED(OCSMSNotificationChannel.DEFAULT, 1),
    PERMISSION(OCSMSNotificationChannel.DEFAULT, 2);

	private final OCSMSNotificationChannel channel;
	private final int notificationId;

	OCSMSNotificationType(OCSMSNotificationChannel channel, int notificationId) {
		this.channel = channel;
		this.notificationId = notificationId;
	}

	public OCSMSNotificationChannel getChannel() {
		return channel;
	}

	public int getNotificationId() {
		return notificationId;
	}
}
