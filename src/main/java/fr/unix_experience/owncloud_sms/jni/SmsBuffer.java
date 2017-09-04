package fr.unix_experience.owncloud_sms.jni;

import fr.unix_experience.owncloud_sms.engine.SmsEntry;
import fr.unix_experience.owncloud_sms.enums.MailboxID;

/**
 *  Copyright (c) 2014-2017, Loic Blot <loic.blot@unix-experience.fr>
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

public class SmsBuffer {
	static {
		System.loadLibrary("nativesms");
	}

	private long mHandle;

	String TAG = SmsBuffer.class.getSimpleName();

	public SmsBuffer() {
		mHandle = SmsBuffer.createNativeObject();
	}

	protected void finalize() throws Throwable {
		clear();
		super.finalize();
	}

	private static native long createNativeObject();
	private native void deleteNativeObject();
	public native void push(int id, int mbid, int type, long date, String address,
								   String body, String read, String seen);
	public native boolean empty();
	public native void print();
	public native String asRawJsonString();

	public void push(MailboxID mbid, SmsEntry smsEntry) {
		push(smsEntry.id,
				mbid.ordinal(),
				smsEntry.type,
				smsEntry.date,
				smsEntry.address,
				smsEntry.body,
				smsEntry.read ? "true" : "false",
				smsEntry.seen ? "true" : "false");
	}

	public void clear() {
		if (mHandle == 0) {
			return;
		}

		deleteNativeObject();
		mHandle = 0;
	}


}
