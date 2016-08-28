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
import org.json.JSONObject;

import fr.unix_experience.owncloud_sms.enums.MailboxID;
import fr.unix_experience.owncloud_sms.providers.SmsDataProvider;

public class SmsFetcher {
	public SmsFetcher(Context ct) {
		_lastMsgDate = (long) 0;
		_context = ct;
		
		_existingInboxMessages = null;
		_existingSentMessages = null;
		_existingDraftsMessages = null;
	}
	
	public void fetchAllMessages(JSONArray result) {
		bufferMailboxMessages(result, MailboxID.INBOX);
		bufferMailboxMessages(result, MailboxID.SENT);
		bufferMailboxMessages(result, MailboxID.DRAFTS);
	}
	
	private void bufferMailboxMessages(JSONArray result, MailboxID mbID) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if ((_context == null) || (mbURI == null)) {
			return;
		}
		
		if ((mbID != MailboxID.INBOX) && (mbID != MailboxID.SENT) &&
                (mbID != MailboxID.DRAFTS)) {
			Log.e(SmsFetcher.TAG,"Unhandled MailboxID " + mbID.ordinal());
			return;
		}

		// We generate a ID list for this message box
		String existingIDs = buildExistingMessagesString(mbID);

		Cursor c = new SmsDataProvider(_context).queryNonExistingMessages(mbURI, existingIDs);

        // Reading mailbox
		if ((c != null) && (c.getCount() > 0)) {
			c.moveToFirst();
			do {
				JSONObject entry = new JSONObject();

				try {
                    String colName;
					for(int idx=0;idx<c.getColumnCount();idx++) {
						colName = c.getColumnName(idx);

						// Id column is must be an integer
                        switch (colName) {
                            case "_id":
                            case "type":
                                entry.put(colName, c.getInt(idx));
                                break;
                            // Seen and read must be pseudo boolean
                            case "read":
                            case "seen":
                                entry.put(colName, (c.getInt(idx) > 0) ? "true" : "false");
                                break;
                            default:
                                // Special case for date, we need to record last without searching
                                if ("date".equals(colName)) {
                                    Long tmpDate = c.getLong(idx);
                                    if (tmpDate > _lastMsgDate) {
                                        _lastMsgDate = tmpDate;
                                    }
                                }
                                entry.put(colName, c.getString(idx));
                                break;
                        }
					}

					// Mailbox ID is required by server
					entry.put("mbox", mbID.ordinal());

                    result.put(entry);

				} catch (JSONException e) {
					Log.e(SmsFetcher.TAG, "JSON Exception when reading SMS Mailbox", e);
					c.close();
				}
			}
			while (c.moveToNext());

			Log.d(SmsFetcher.TAG, c.getCount() + " messages read from " + mbURI);

			c.close();
		}
	}
	
	// Used by Content Observer
	public JSONArray getLastMessage(MailboxID mbID) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if ((_context == null) || (mbURI == null)) {
			return null;
		}
		
		// Fetch Sent SMS Message from Built-in Content Provider
		Cursor c = (new SmsDataProvider(_context)).query(mbURI);
		
		c.moveToNext();
		
		// We create a list of strings to store results
		JSONArray results = new JSONArray();
		
		JSONObject entry = new JSONObject();

		try {
			Integer mboxId = -1;
            String colName;
			for(int idx = 0;idx < c.getColumnCount(); idx++) {
				colName = c.getColumnName(idx);
				
				// Id column is must be an integer
                switch (colName) {
                    case "_id":
                        entry.put(colName, c.getInt(idx));
                        break;
                    // Seen and read must be pseudo boolean
                    case "read":
                    case "seen":
                        entry.put(colName, (c.getInt(idx) > 0) ? "true" : "false");
                        break;
                    case "type":
                        mboxId = c.getInt(idx);
                        entry.put(colName, c.getInt(idx));
                        break;
                    default:
                        entry.put(colName, c.getString(idx));
                        break;
                }
			}
			
			/*
			* Mailbox ID is required by server
			* mboxId is greater than server mboxId by 1 because types 
			* aren't indexed in the same mean
			*/
			entry.put("mbox", (mboxId - 1));
			
			results.put(entry);
		} catch (JSONException e) {
			Log.e(SmsFetcher.TAG, "JSON Exception when reading SMS Mailbox", e);
			c.close();
		}
		
		c.close();
		
		return results;
	}
	
	// Used by ConnectivityChanged Event
	public void bufferMessagesSinceDate(JSONArray result, Long sinceDate) {
		bufferMessagesSinceDate(result, MailboxID.INBOX, sinceDate);
		bufferMessagesSinceDate(result, MailboxID.SENT, sinceDate);
		bufferMessagesSinceDate(result, MailboxID.DRAFTS, sinceDate);
	}
	
	// Used by ConnectivityChanged Event
	public void bufferMessagesSinceDate(JSONArray result, MailboxID mbID, Long sinceDate) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if ((_context == null) || (mbURI == null)) {
			return;
		}
		
		Cursor c = new SmsDataProvider(_context).queryMessagesSinceDate(mbURI, sinceDate);
		
		// Reading mailbox
		if ((c != null) && (c.getCount() > 0)) {
			c.moveToFirst();
			do {
				JSONObject entry = new JSONObject();

				try {
                    String colName;
					for (int idx = 0; idx < c.getColumnCount(); idx++) {
						colName = c.getColumnName(idx);
                        switch (colName) {
                            // Id column is must be an integer
                            case "_id":
                            case "type":
                                entry.put(colName, c.getInt(idx));
                                break;
                            // Seen and read must be pseudo boolean
                            case "read":
                            case "seen":
                                entry.put(colName, (c.getInt(idx) > 0) ? "true" : "false");
                                break;
                            default:
                                // Special case for date, we need to record last without searching
                                if ("date".equals(colName)) {
                                    Long tmpDate = c.getLong(idx);
                                    if (tmpDate > _lastMsgDate) {
                                        _lastMsgDate = tmpDate;
                                    }
                                }
                                entry.put(colName, c.getString(idx));
                                break;
                        }
					}
					
					// Mailbox ID is required by server
					entry.put("mbox", mbID.ordinal());

                    result.put(entry);
					
				} catch (JSONException e) {
					Log.e(SmsFetcher.TAG, "JSON Exception when reading SMS Mailbox", e);
					c.close();
				}
			}
			while (c.moveToNext());
			
			Log.d(SmsFetcher.TAG, c.getCount() + " messages read from " + mbURI);
			
			c.close();
		}
	}
	
	private String mapMailboxIDToURI(MailboxID mbID) {
		if (mbID == MailboxID.INBOX) {
			return "content://sms/inbox";
		}
		else if (mbID == MailboxID.DRAFTS) {
			return "content://sms/drafts";
		}
		else if (mbID == MailboxID.SENT) {
			return "content://sms/sent";
		}
		else if (mbID == MailboxID.ALL) {
			return "content://sms";
		}
		
		return null;
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
	
	private final Context _context;
	private JSONArray _existingInboxMessages;
	private JSONArray _existingSentMessages;
	private JSONArray _existingDraftsMessages;
	
	private Long _lastMsgDate;
	
	private static final String TAG = SmsFetcher.class.getSimpleName();
}
