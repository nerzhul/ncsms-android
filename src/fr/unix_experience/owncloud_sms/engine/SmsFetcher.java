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
		_jsonDataDump = new JSONArray();
		bufferizeMailboxMessages(MailboxID.INBOX);
		bufferizeMailboxMessages(MailboxID.SENT);
		bufferizeMailboxMessages(MailboxID.DRAFTS);
		return _jsonDataDump;
	}
	
	private void bufferizeMailboxMessages(MailboxID mbID) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if (_context == null || mbURI == null) {
			return;
		}
		
		if (mbID != MailboxID.INBOX && mbID != MailboxID.SENT &&
			mbID != MailboxID.DRAFTS) {
			Log.e(TAG,"Unhandled MailboxID " + mbID.ordinal());
			return;
		}

		// We generate a ID list for this message box
		String existingIDs = buildExistingMessagesString(mbID);
		
		Cursor c = null;
		if (existingIDs.length() > 0) {
			c = (new SmsDataProvider(_context)).query(mbURI, "_id NOT IN (" + existingIDs + ")");
		}
		else {
			c = (new SmsDataProvider(_context)).query(mbURI);
		}
		
		// Reading mailbox
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			do {
				JSONObject entry = new JSONObject();

				try {
					for(int idx=0;idx<c.getColumnCount();idx++) {
						String colName = c.getColumnName(idx);
						
						// Id column is must be an integer
						if (colName.equals(new String("_id")) ||
							colName.equals(new String("type"))) {
							entry.put(colName, c.getInt(idx));
							
							// bufferize Id for future use
							if (colName.equals(new String("_id"))) {
							}
						}
						// Seen and read must be pseudo boolean
						else if (colName.equals(new String("read")) ||
								colName.equals(new String("seen"))) {
							entry.put(colName, c.getInt(idx) > 0 ? "true" : "false");
						}
						else {
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
					
					// Mailbox ID is required by server
					entry.put("mbox", mbID.ordinal());
					
					_jsonDataDump.put(entry);
					
				} catch (JSONException e) {
					Log.e(TAG, "JSON Exception when reading SMS Mailbox", e);
					c.close();
				}
			}
			while(c.moveToNext());
			
			Log.d(TAG, c.getCount() + " messages read from " + mbURI);
			
			c.close();
		}
	}
	
	// Used by Content Observer
	public JSONArray getLastMessage(MailboxID mbID) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if (_context == null || mbURI == null) {
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
			for(int idx = 0;idx < c.getColumnCount(); idx++) {
				String colName = c.getColumnName(idx);
				
				// Id column is must be an integer
				if (colName.equals(new String("_id")) ||
					colName.equals(new String("type"))) {
					entry.put(colName, c.getInt(idx));
				}
				// Seen and read must be pseudo boolean
				else if (colName.equals(new String("read")) ||
						colName.equals(new String("seen"))) {
					entry.put(colName, c.getInt(idx) > 0 ? "true" : "false");
				}
				else if (colName.equals(new String("type"))) {
					mboxId = c.getInt(idx);
				}
				else {
					entry.put(colName, c.getString(idx));
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
			Log.e(TAG, "JSON Exception when reading SMS Mailbox", e);
			c.close();
		}
		
		c.close();
		
		return results;
	}
	
	// Used by ConnectivityChanged Event
	public JSONArray bufferizeMessagesSinceDate(Long sinceDate) {
		_jsonDataDump = new JSONArray();
		bufferizeMessagesSinceDate(MailboxID.INBOX, sinceDate);
		bufferizeMessagesSinceDate(MailboxID.SENT, sinceDate);
		bufferizeMessagesSinceDate(MailboxID.DRAFTS, sinceDate);
		return _jsonDataDump;
	}
	
	// Used by ConnectivityChanged Event
	public void bufferizeMessagesSinceDate(MailboxID mbID, Long sinceDate) {
		String mbURI = mapMailboxIDToURI(mbID);
		
		if (_context == null || mbURI == null) {
			return;
		}
		
		Cursor c = new SmsDataProvider(_context).query(mbURI, "date > ?", new String[] { sinceDate.toString() });
		
		// Reading mailbox
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			do {
				JSONObject entry = new JSONObject();

				try {
					for(int idx=0;idx<c.getColumnCount();idx++) {
						String colName = c.getColumnName(idx);
						
						// Id column is must be an integer
						if (colName.equals(new String("_id")) ||
							colName.equals(new String("type"))) {
							entry.put(colName, c.getInt(idx));
							
							// bufferize Id for future use
							if (colName.equals(new String("_id"))) {
							}
						}
						// Seen and read must be pseudo boolean
						else if (colName.equals(new String("read")) ||
								colName.equals(new String("seen"))) {
							entry.put(colName, c.getInt(idx) > 0 ? "true" : "false");
						}
						else {
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
					
					// Mailbox ID is required by server
					entry.put("mbox", mbID.ordinal());
					
					_jsonDataDump.put(entry);
					
				} catch (JSONException e) {
					Log.e(TAG, "JSON Exception when reading SMS Mailbox", e);
					c.close();
				}
			}
			while(c.moveToNext());
			
			Log.d(TAG, c.getCount() + " messages read from " + mbURI);
			
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
		// Note: The default case isn't possible, we check the mailbox before
		
		StringBuilder sb = new StringBuilder();
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
	private JSONArray _jsonDataDump;
	private JSONArray _existingInboxMessages;
	private JSONArray _existingSentMessages;
	private JSONArray _existingDraftsMessages;
	
	private Long _lastMsgDate;
	
	private static final String TAG = SmsFetcher.class.getSimpleName();
}
