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

public class SmsDataProvider extends ContentProvider {
	public SmsDataProvider () {}
	
	public SmsDataProvider (Context ct) {
		super();
		_context = ct;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	public Cursor query(String mailBox) {
		return query(Uri.parse(mailBox),
			new String[] { "read", "date", "address", "seen", "body", "_id", "type", },
			null, null, null
		);
	}
	
	public Cursor query(String mailBox, String selection) {
		return query(Uri.parse(mailBox),
			new String[] { "read", "date", "address", "seen", "body", "_id", "type", },
			selection, null, null
		);
	}
	
	public Cursor query(String mailBox, String selection, String[] selectionArgs) {
		return query(Uri.parse(mailBox),
			new String[] { "read", "date", "address", "seen", "body", "_id", "type", },
			selection, selectionArgs, null
		);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (_context != null && _context.getContentResolver() != null) {
			return _context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
		}
		
		return null;
	}
	
	

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Context _context;
}
