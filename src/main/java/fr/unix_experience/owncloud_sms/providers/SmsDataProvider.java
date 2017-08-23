package fr.unix_experience.owncloud_sms.providers;

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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import fr.unix_experience.owncloud_sms.enums.MailboxID;
import fr.unix_experience.owncloud_sms.prefs.OCSMSSharedPrefs;

public class SmsDataProvider extends ContentProvider {
	static String[] messageFields = {
			"read",
			"date",
			"address",
			"seen",
			"body",
			"_id",
			"type",
			//"length(address)" // For debug purposes
	};

    // WARNING: mandatory
    public SmsDataProvider() {}
    public SmsDataProvider (Context ct) {
		super();
		_context = ct;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	public Cursor query(String mailBox) {
		return query(Uri.parse(mailBox),
				SmsDataProvider.messageFields,
				null, null, null
				);
	}

    // NOTE: in APIv2 this call should be modified to use date instead of _id which is likely unique
	public Cursor queryNonExistingMessages(String mailBox, String existingIds) {
        Log.i(SmsDataProvider.TAG, "queryNonExistingMessages !");
		if (!existingIds.isEmpty()) {
            return query(Uri.parse(mailBox), SmsDataProvider.messageFields,
                    "_id NOT IN (" + existingIds + ")", null, null
            );
		}

		return query(mailBox);
	}

    public Cursor queryMessagesSinceDate(String mailBox, Long sinceDate) {
        return query(Uri.parse(mailBox), SmsDataProvider.messageFields,
                "date > ?", new String[] { sinceDate.toString() }, null
        );
    }

	public boolean messageExists(String address, String body, String date, MailboxID mb) {
		Cursor c = query(Uri.parse(mb.getURI()), new String[] { "_id" },
				"address = ? AND body = ? AND date = ?",
				new String[] { address, body, date }, null);
		if (c == null) {
			return false;
		}

		boolean exists = c.getCount() > 0;
		c.close();
		return exists;
	}

	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        if ((_context == null) || (_context.getContentResolver() == null)) {
            Log.e(SmsDataProvider.TAG, "query: context is null or content resolver is null, abort.");
            return null;
        }
        OCSMSSharedPrefs prefs = new OCSMSSharedPrefs(_context);
        Integer bulkLimit = prefs.getSyncBulkLimit();
        Integer senderMinSize = prefs.getMinPhoneNumberCharsToSync();
        if (senderMinSize < 0) {
            senderMinSize = 0;
        }

        // If minSize > 0 we should filter
        if (senderMinSize > 0) {
            selection = ((selection == null) || (selection.isEmpty())) ?
                    ("length(address) >= " + senderMinSize.toString()) :
                    ("length(address) >= " + senderMinSize.toString() + " AND " + selection);
        }

        if (bulkLimit > 0) {
            if (sortOrder == null)
                sortOrder = "date DESC";
            sortOrder += " LIMIT " + bulkLimit.toString();
        }

        Cursor cursor = _context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}

		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		return cursor;
	}

	@Override
	public String getType(@NonNull Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Context _context;
    private static final String TAG = SmsDataProvider.class.getSimpleName();
}
