/**************************************************************************
 * Copyright 2009 Chris Thompson                                           *
 *                                                                         *
 * Licensed under the Apache License, Version 2.0 (the "License");         *
 * you may not use this file except in compliance with the License.        *
 * You may obtain a copy of the License at                                 *
 *                                                                         *
 * http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                         *
 * Unless required by applicable law or agreed to in writing, software     *
 * distributed under the License is distributed on an "AS IS" BASIS,       *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.*
 * See the License for the specific language governing permissions and     *
 * limitations under the License.                                          *
 **************************************************************************/
package org.eece261.webtext.utils;

import org.eece261.webtext.SMS;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;

public class SMSUtils {

	public static final Uri MMS_SMS_CONTENT_URI = Uri.parse("content://mms-sms/");
	public static final Uri THREAD_ID_CONTENT_URI =
		Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "threadID");
	public static final Uri CONVERSATION_CONTENT_URI =
		Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "conversations");

	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	public static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
	public static final Uri MMS_INBOX_CONTENT_URI = Uri.withAppendedPath(MMS_CONTENT_URI, "inbox");

	public static final String SMS_ID = "_id";
	public static final String SMS_TO_URI = "smsto:/";
	public static final String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
	public static final int READ_THREAD = 1;
	public static final int MESSAGE_TYPE_SMS = 1;
	public static final int MESSAGE_TYPE_MMS = 2;

	private static final String TIME_FORMAT_12_HOUR = "h:mm a";
	private static final String TIME_FORMAT_24_HOUR = "H:mm";

	public static String getPersonIdFromPhoneNumber(ContentResolver resolver, String addr){
		if (addr == null)
			return null;

		Cursor cursor = resolver.query(
				Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, addr), new String[] { Contacts.Phones.PERSON_ID }, null, null, null);

		if (cursor != null){
			try{
				if (cursor.getCount() > 0){
					cursor.moveToLast();
					Long id = Long.valueOf(cursor.getLong(0));
					return (String.valueOf(id));
				}
			}finally {
				cursor.close();
			}
		}
		return null;
	}

	public static SMS getSmsDetails(Context context,
			long ignoreThreadId, boolean unreadOnly) {

		String SMS_READ_COLUMN = "read";
		String WHERE_CONDITION = unreadOnly ? SMS_READ_COLUMN + " = 0" : null;
		String SORT_ORDER = "date DESC";
		int count = 0;

		if (ignoreThreadId > 0) {
			WHERE_CONDITION += " AND thread_id != " + ignoreThreadId;
		}

		Cursor cursor = context.getContentResolver().query(
				SMS_INBOX_CONTENT_URI,
				new String[] { "_id", "thread_id", "address", "person", "date", "body" },
				WHERE_CONDITION,
				null,
				SORT_ORDER);

		if (cursor != null) {
			try {
				count = cursor.getCount();
				if (count > 0) {
					cursor.moveToFirst();
					long messageId = cursor.getLong(0);
					long threadId = cursor.getLong(1);
					String address = cursor.getString(2);
					long contactId = cursor.getLong(3);
					String contactId_string = String.valueOf(contactId);
					long timestamp = cursor.getLong(4);

					String body = cursor.getString(5);

					if (!unreadOnly) {
						count = 0;
					}



				}
			} finally {
				cursor.close();
			}
		}               
		return null;
	}


}
