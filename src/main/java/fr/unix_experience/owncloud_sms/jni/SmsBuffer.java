package fr.unix_experience.owncloud_sms.jni;

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

	public SmsBuffer() {
		mHandle = SmsBuffer.createNativeObject();
	}

	protected void finalize() throws Throwable {
		SmsBuffer.deleteNativeObject(mHandle);
		mHandle = 0;
		super.finalize();
	}

	private static native long createNativeObject();
	private static native void deleteNativeObject(long handle);

	public static native void push(long handle, int mbid);
	public void push(int mbid) {
		SmsBuffer.push(mHandle, mbid);
	}

	public static native void print(long handle);
	public void print() {
		SmsBuffer.print(mHandle);
	}
}
