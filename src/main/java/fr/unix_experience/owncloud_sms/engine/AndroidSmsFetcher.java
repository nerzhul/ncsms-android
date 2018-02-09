package fr.unix_experience.owncloud_sms.engine;

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
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import fr.unix_experience.owncloud_sms.enums.MailboxID;
import fr.unix_experience.owncloud_sms.providers.SmsDataProvider;
import ncsmsgo.SmsBuffer;

public class AndroidSmsFetcher {
	public AndroidSmsFetcher(Context ct) {
		_context = ct;

		_existingInboxMessages = null;
		_existingSentMessages = null;
		_existingDraftsMessages = null;
	}

	void fetchAllMessages(SmsBuffer result) {
		bufferMailboxMessages(result, MailboxID.INBOX);
		bufferMailboxMessages(result, MailboxID.SENT);
		bufferMailboxMessages(result, MailboxID.DRAFTS);
	}

	private void readMailBox(Cursor c, SmsBuffer smsBuffer, MailboxID mbID) {
		do {
			SmsEntry entry = new SmsEntry();

			for (int idx = 0; idx < c.getColumnCount(); idx++) {
				handleProviderColumn(c, idx, entry);
			}

			// Mailbox ID is required by server
			entry.mailboxId = mbID.ordinal();
			smsBuffer.push(entry.id,
					mbID.ordinal(),
					entry.type,
					entry.date,
					entry.address,
					entry.body,
					entry.read ? "true" : "false",
					entry.seen ? "true" : "false");
		}
		while (c.moveToNext());
	}

	private void bufferMailboxMessages(SmsBuffer smsBuffer, MailboxID mbID) {
		if ((_context == null)) {
			return;
		}

		if ((mbID != MailboxID.INBOX) && (mbID != MailboxID.SENT) &&
				(mbID != MailboxID.DRAFTS)) {
			Log.e(AndroidSmsFetcher.TAG, "Unhandled MailboxID " + mbID.ordinal());
			return;
		}

		// We generate a ID list for this message box
		String existingIDs = buildExistingMessagesString(mbID);
		Cursor c = new SmsDataProvider(_context).queryNonExistingMessages(mbID.getURI(), existingIDs);

		if (c == null) {
			return;
		}

		// Reading mailbox
		readMailBox(c, smsBuffer, mbID);

		Log.i(AndroidSmsFetcher.TAG, c.getCount() + " messages read from " + mbID.getURI());
		c.close();
	}

	// Used by Content Observer
	public SmsBuffer getLastMessage(MailboxID mbID) {
		if ((_context == null)) {
			return null;
		}

		// Fetch Sent SMS Message from Built-in Content Provider
		Cursor c = (new SmsDataProvider(_context)).query(mbID.getURI());
		if (c == null) {
			return null;
		}

		// We create a list of strings to store results
		SmsEntry entry = new SmsEntry();
		SmsBuffer results = new SmsBuffer();

		Integer mboxId = -1;
		for (int idx = 0; idx < c.getColumnCount(); idx++) {
			Integer rid = handleProviderColumn(c, idx, entry);
			if (rid != -1) {
				mboxId = rid;
			}
		}

		/*
		* Mailbox ID is required by server
		* mboxId is greater than server mboxId by 1 because types
		* aren't indexed in the same mean
		*/
		entry.mailboxId = mboxId - 1;
		results.push(entry.id,
				mbID.ordinal(),
				entry.type,
				entry.date,
				entry.address,
				entry.body,
				entry.read ? "true" : "false",
				entry.seen ? "true" : "false");

		c.close();

		return results;
	}

	// Used by ConnectivityChanged Event
	public void bufferMessagesSinceDate(SmsBuffer smsBuffer, Long sinceDate) {
		bufferMessagesSinceDate(smsBuffer, MailboxID.INBOX, sinceDate);
		bufferMessagesSinceDate(smsBuffer, MailboxID.SENT, sinceDate);
		bufferMessagesSinceDate(smsBuffer, MailboxID.DRAFTS, sinceDate);
	}

	// Used by ConnectivityChanged Event
	private void bufferMessagesSinceDate(SmsBuffer smsBuffer, MailboxID mbID, Long sinceDate) {
		Log.i(AndroidSmsFetcher.TAG, "bufferMessagesSinceDate for " + mbID.toString() + " sinceDate " + sinceDate.toString());
		if ((_context == null)) {
			return;
		}

		Cursor c = new SmsDataProvider(_context).queryMessagesSinceDate(mbID.getURI(), sinceDate);
		if (c != null) {
			Log.i(AndroidSmsFetcher.TAG, "Retrieved " + c.getCount() + " messages.");
		} else {
			Log.i(AndroidSmsFetcher.TAG, "No message retrieved.");
			return;
		}

		// Read Mailbox
		readMailBox(c, smsBuffer, mbID);

		Log.i(AndroidSmsFetcher.TAG, c.getCount() + " messages read from " + mbID.getURI());
		c.close();
	}

	private Integer handleProviderColumn(Cursor c, int idx, SmsEntry entry) {
		String colName = c.getColumnName(idx);

		// Id column is must be an integer
		switch (colName) {
			case "_id":
				entry.id = c.getInt(idx);
				break;
			case "type":
				entry.type = c.getInt(idx);
				return c.getInt(idx);
			/* For debug purpose
            case "length(address)":
                Log.i(AndroidSmsFetcher.TAG, "Column name " + colName + " " + c.getString(idx));
                break;*/
			// Seen and read must be pseudo boolean
			case "read":
				entry.read = (c.getInt(idx) > 0);
				break;
			case "seen":
				entry.seen = (c.getInt(idx) > 0);
				break;
			case "date":
				entry.date = c.getLong(idx);
				break;
			case "address":
				entry.address = c.getString(idx);
				break;
			case "body":
				entry.body = c.getString(idx);
				break;
			default:
				// Unhandled column
				break;
		}

		return -1;
	}

	private String buildExistingMessagesString(MailboxID _mbID) {
		JSONArray existingMessages = null;
		if (_mbID == MailboxID.INBOX) {
			existingMessages = _existingInboxMessages;
		} else if (_mbID == MailboxID.DRAFTS) {
			existingMessages = _existingDraftsMessages;
		} else if (_mbID == MailboxID.SENT) {
			existingMessages = _existingSentMessages;
		}

		if (existingMessages == null) {
			return "";
		}

		// Note: The default case isn't possible, we check the mailbox before
		StringBuilder sb = new StringBuilder();
		int len = existingMessages.length();
		for (int i = 0; i < len; i++) {
			try {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(existingMessages.getInt(i));
			} catch (JSONException ignored) {

			}
		}

		return sb.toString();
	}

	void setExistingInboxMessages(JSONArray inboxMessages) {
		_existingInboxMessages = inboxMessages;
	}

	void setExistingSentMessages(JSONArray sentMessages) {
		_existingSentMessages = sentMessages;
	}
	void setExistingDraftsMessages(JSONArray draftMessages) {
		_existingDraftsMessages = draftMessages;
	}

	private final Context _context;
	private JSONArray _existingInboxMessages;
	private JSONArray _existingSentMessages;
	private JSONArray _existingDraftsMessages;

	private static final String TAG = AndroidSmsFetcher.class.getSimpleName();
}
