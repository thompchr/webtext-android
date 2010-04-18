package org.webtext.android;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.telephony.gsm.SmsManager;

import com.thoughtworks.xstream.XStream;

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

public class TextServlet extends HttpServlet {


	private final String CONVERSATION_URI = "content://sms/conversations";
	private final String ALL_URI = "content://sms";

	private static final int ALL_CONVERSATIONS = 0;
	private static final int UPDATE_THREAD = 1;
	private static final int SEND_MESSAGE = 2;

	private static final String ID = "_id";
	private static final String THREAD_ID = "thread_id";
	private static final String ADDRESS = "address";
	private static final String PERSON = "person";
	private static final String DATE = "date";
	private static final String BODY = "body";
	private static final String TYPE = "type";


	private ContentResolver resolver_;

	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		resolver_ = (ContentResolver)getServletContext().getAttribute(WebText.CONTENT_RESOLVER_ATTRIBUTE);
	}

	public ContentResolver getContentResolver(){
		return resolver_;
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		doGet(request, response);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		
		Uri sms;
		Cursor c;
		String type = request.getParameter("type");
		if (type == null)
			return;
		switch (Integer.parseInt(type)){

		case ALL_CONVERSATIONS:
			sms = Uri.parse(ALL_URI);
			c = getContentResolver().query(sms, new String[] { ID, THREAD_ID, ADDRESS, PERSON, DATE, BODY, TYPE }, null, null, null);
			HashMap<Integer, Thread> threads = new HashMap<Integer, Thread>();

			if (c != null){
				try{
					int count = c.getCount();
					if (count > 0){
						c.moveToFirst();
						for (int i = 0; i < count; ++i){
							String dir;
							if (c.getInt(6) == 2)
								dir = "out";
							else
								dir = "in";
							if (threads.containsKey(c.getInt(1))){

								threads.get(c.getInt(1)).addSMS(new SMS (c.getInt(0), c.getString(5), c.getLong(4), dir));
							}else{
								Thread trd = new Thread(c.getInt(1));
								trd.addSMS(new SMS(c.getInt(0), c.getString(5), c.getLong(4), dir));
								trd.setAddress(c.getString(2));
								trd.setContactName(getContactName(c.getInt(3)));
								threads.put(c.getInt(1), trd);
							}
							c.moveToNext();
						}
					}
				}finally{
					c.close();
				}
			}

			ArrayList<Thread> temp = new ArrayList<Thread>();
			for (Thread trd : threads.values()){
				temp.add(trd);
			}
			for (Thread trd : temp){
				trd.sort(Thread.Order.Descending);
			}
			TempContainer tc = new TempContainer(temp);
			XStream xstream = new XStream();

			xstream.addImplicitCollection(TempContainer.class, "threads_");
			xstream.addImplicitCollection(Thread.class, "thread_");
			xstream.alias("sms", SMS.class);
			xstream.aliasField("id", SMS.class, "id_");
			xstream.aliasField("msg", SMS.class, "body_");
			xstream.aliasField("dt", SMS.class, "date_");
			xstream.aliasField("contact", Thread.class, "contactName_");
			xstream.aliasField("id", Thread.class, "id_");
			xstream.aliasField("addr", Thread.class, "address_");
			xstream.alias("Thread", Thread.class);
			xstream.alias("Threads", TempContainer.class);
			xstream.aliasField("dir", SMS.class, "direction_");

			response.getWriter().write(xstream.toXML(tc));
			break;

		case UPDATE_THREAD:

			sms = Uri.withAppendedPath(Uri.parse(CONVERSATION_URI), request.getParameter("thread"));
			c = getContentResolver().query(sms, new String[] { ID, THREAD_ID, ADDRESS, PERSON, DATE, BODY, TYPE }, null, null, null);
			Thread trd;
			String dir;

			if (c != null){
				try{
					int count = c.getCount();

					if (count > 0){
						c.moveToFirst();
						if (c.getInt(6) == 2)
							dir = "out";
						else
							dir = "in";
						trd = new Thread(c.getInt(1));
						trd.setAddress(c.getString(2));
						trd.setContactName(getContactName(c.getInt(3)));
						for (int i = 0; i < count; ++i){
							trd.addSMS(new SMS (c.getInt(0), c.getString(5), c.getLong(4), dir));
						}
						c.moveToNext();
					}
				}finally{
					c.close();
				}
			}
			break;

		case SEND_MESSAGE:

			String destination = request.getParameter("dest");
			String message = request.getParameter("msg");

			if (message != null && destination != null){

				SmsManager sm = SmsManager.getDefault();
				ContentValues cv = new ContentValues();
				cv.put(THREAD_ID, request.getParameter("trd"));
				cv.put(ADDRESS, destination);

				cv.put(DATE, System.currentTimeMillis());
				cv.put(BODY, message);
				cv.put(TYPE, 2);
				
				getContentResolver().insert(Uri.parse(ALL_URI), cv);

				sm.sendTextMessage(destination, null, message, null, null);
				response.getWriter().write("Message sent");
			}

		}

	}

	private String getContactName (int address){
		Cursor ccurs = getContentResolver().query(Uri.withAppendedPath(People.CONTENT_URI, String.valueOf(address)), 
				new String[] { People.DISPLAY_NAME }, null, null, null);
		try{
			int cnt = ccurs.getCount();
			if (cnt > 0){
				ccurs.moveToFirst();
				return ccurs.getString(0);
			}
		}finally{
			ccurs.close();
		}
		return null;
	}



}
