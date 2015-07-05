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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.unix_experience.owncloud_sms.enums.MailboxID;
import fr.unix_experience.owncloud_sms.providers.SmsDataProvider;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class SmsFetcher {
	public SmsFetcher(Context ct) {
		_lastMsgDate = (long) 0;
		_context = ct;

		_existingInboxMessages = null;
		_existingSentMessages = null;
		_existingDraftsMessages = null;
	}

	public JSONArray fetchAllMessages() {
		JSONArray jsonDataDump = new JSONArray();
		addMailboxMessagesToJson(jsonDataDump, MailboxID.INBOX, -1);
		addMailboxMessagesToJson(jsonDataDump, MailboxID.SENT, -1);
		addMailboxMessagesToJson(jsonDataDump, MailboxID.DRAFTS, -1);
		return jsonDataDump;
	}

	private void addMailboxMessagesToJson(JSONArray jsonDataDump, MailboxID mbID, Long sinceDate) {
		// called by fetchAllMessages()				with sinceDate == -1
		// called by getLastMessage()				with sinceDate == -2
		// called by bufferizeMessagesSinceDate()	with sinceDate > 0
		// calling with mbID == MailboxID.ALL and sinceDate == 0 would load ALL messages in ALL mailboxes

		if (_context == null) {
			return null;
		}

		String mbURI = mapMailboxIDToURI(mbID);

		if (mbURI == null) {
			Log.e(TAG, "Unhandled MailboxID " + mbID.ordinal());
			return null;
		}

		String existingIDs = null;
		if (sinceDate == (long) -1) {
			// We generate a ID list for this message box
			existingIDs = buildExistingMessagesString(mbID);
		}

		Cursor c = null;

		if (existingIDs.length() > 0) {
			c = (new SmsDataProvider(_context)).query(mbURI, "_id NOT IN (" + existingIDs + ")");
		} else if sinceDate > 0){
			c = (new SmsDataProvider(_context)).query(mbURI, "date > ?", new String[]{sinceDate.toString()});
		}
		else{
			c = (new SmsDataProvider(_context)).query(mbURI);
		}

		// Reading mailbox
		if (c != null && c.getCount() > 0) {

			if (sinceDate == -2) {
				// When called by getLastMessage() then we move to the next message
				c.moveToNext();
			} else {
				c.moveToFirst();
			}

			Integer mboxId = -1;

			do {
				JSONObject entry = new JSONObject();

				try {
					for (int idx = 0; idx < c.getColumnCount(); idx++) {
						String colName = c.getColumnName(idx);

						// Id column is must be an integer
						if (colName.equals(new String("_id"))) {
							entry.put(colName, c.getInt(idx));
						}
						// Seen and read must be pseudo boolean
						else if (colName.equals(new String("read")) ||
								colName.equals(new String("seen"))) {
							entry.put(colName, c.getInt(idx) > 0 ? "true" : "false");
						} else if (colName.equals(new String("type"))) {
							mboxId = c.getInt(idx);
							entry.put(colName, c.getInt(idx));
						} else {
							// Special case for date, we need to record last without searching
							if (colName.equals(new String("date"))) {
								final Long tmpDate = c.getLong(idx);
								if (tmpDate > _lastMsgDate) {
									_lastMsgDate = tmpDate;
								}
							}
							entry.put(colName, c.getString(idx));
						}
					}

					/*
					* Mailbox ID is required by server
					* mboxId is greater than server mboxId by 1 because types
					* aren't indexed in the same mean
					*/
					entry.put("mbox", (mboxId - 1));

					jsonDataDump.put(entry);

				} catch (JSONException e) {
					Log.e(TAG, "JSON Exception when reading SMS Mailbox", e);
					c.close();
				}
			}
			while (sinceDate != -2 && c.moveToNext());

			Log.d(TAG, c.getCount() + " messages read from " + mbURI);

			c.close();
		}
	}

	// Used by Content Observer
	public JSONArray getLastMessage(MailboxID mbID) {
		JSONArray jsonDataDump = new JSONArray();

		if (addMailboxMessagesToJson(jsonDataDump, mbID, -2) == null) {
			return null;
		}

		return jsonDataDump;
	}

	// Used by ConnectivityChanged Event
	public JSONArray bufferizeMessagesSinceDate(Long sinceDate) {
		JSONArray jsonDataDump = new JSONArray();
		addMailboxMessagesToJson(jsonDataDump, MailboxID.INBOX, sinceDate);
		addMailboxMessagesToJson(jsonDataDump, MailboxID.SENT, sinceDate);
		addMailboxMessagesToJson(jsonDataDump, MailboxID.DRAFTS, sinceDate);
		return jsonDataDump;
	}

	private String mapMailboxIDToURI(MailboxID mbID) {
		if (mbID == MailboxID.INBOX) {
			return "content://sms/inbox";
		} else if (mbID == MailboxID.DRAFTS) {
			return "content://sms/drafts";
		} else if (mbID == MailboxID.SENT) {
			return "content://sms/sent";
		} else if (mbID == MailboxID.ALL) {
			return "content://sms";
		}

		return null;
	}

	private String buildExistingMessagesString(MailboxID mbID) {
		JSONArray existingMessages = null;

		StringBuilder sb = new StringBuilder();

		for (MailboxID idx : MailboxID.values()) {
			if (mbID == idx || mbID == MailboxID.ALL)){
				existingMessages = _existingInboxMessages;
			}else if (mbID == idx || mbID == MailboxID.ALL)){
				existingMessages = _existingDraftsMessages;
			}else if (mbID == idx || mbID == MailboxID.ALL)){
				existingMessages = _existingSentMessages;
			}else{
				existingMessages = null;
			}

			if (existingMessages != null) {
				int len = existingMessages.length();
				for (int i = 0; i < len; i++) {
					try {
						if (sb.length() > 0) {
							sb.append(",");
						}
						sb.append(existingMessages.getInt(i));
					} catch (JSONException e) {

					}
				}
			}
		}

		return sb.toString();
	}

	public void setExistingInboxMessages(JSONArray inboxMessages) {
		_existingInboxMessages = inboxMessages;
	}

	public void setExistingSentMessages(JSONArray sentMessages) {
		_existingSentMessages = sentMessages;
	}

	public void setExistingDraftsMessages(JSONArray draftMessages) {
		_existingDraftsMessages = draftMessages;
	}

	public Long getLastMessageDate() {
		return _lastMsgDate;
	}

	private Context _context;
	private JSONArray _existingInboxMessages;
	private JSONArray _existingSentMessages;
	private JSONArray _existingDraftsMessages;

	private Long _lastMsgDate;

	private static final String TAG = SmsFetcher.class.getSimpleName();
}
