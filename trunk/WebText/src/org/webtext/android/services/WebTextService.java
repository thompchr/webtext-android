/**************************************************************************
 * Copyright 2010 Chris Thompson                                           *
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
package org.webtext.android.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.Client;
import org.cometd.Message;
import org.cometd.SecurityPolicy;
import org.mortbay.cometd.continuation.ContinuationCometdServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.webtext.android.TextServlet;
import org.webtext.android.WebText;
import org.webtext.android.push.SmsPush;
import org.webtext.android.services.bindings.IWebTextService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsMessage;
import android.util.Log;

public class WebTextService extends Service {


	private int port_ = 8080;
	private Server server_;
	private boolean isRunning_ = false;
	private static final String TAG = "WebTextService";
	
	private int testHandshakeFailure_;

	private static final String ACTION_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTION_SEND = "android.provider.Telephone.SMS_SENT";

	private ContinuationCometdServlet comet_servlet_ = new ContinuationCometdServlet();

	private BroadcastReceiver receiver_ = new BroadcastReceiver(){

		@Override
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_RECEIVE) || intent.getAction().equals(ACTION_SEND)){
				Bundle bundle = intent.getExtras();
				Log.v(TAG, "Received broadcast intent: " + intent.getAction());

				Object[] pdus = (Object[]) bundle.get("pdus");
				List<SmsPush> msgs = new ArrayList<SmsPush>();

				for (int i = 0; i < pdus.length; ++i){
					msgs.add(new SmsPush(SmsMessage.createFromPdu((byte[]) pdus[i])));
				}

				pushMessage(msgs);

			}
		}

	};

	private final IWebTextService.Stub binder_ = new IWebTextService.Stub(){


		@Override
		public void startServer() throws RemoteException {
			Log.d(TAG, "Server start requested");
			if (!isRunning_){
				try {
					server_.start();
					//ushService.launch(comet_servlet_.getBayeux());
					comet_servlet_.getBayeux().setSecurityPolicy(new SecurityPolicy() {
						
						@Override
						public boolean canSubscribe(Client arg0, String arg1, Message arg2) {
							
							return true;
						}
						
						@Override
						public boolean canPublish(Client arg0, String arg1, Message arg2) {
							// TODO Auto-generated method stub
							return true;
						}
						
						@Override
						public boolean canHandshake(Message arg0) {
							// TODO Auto-generated method stub
							return true;
						}
						
						@Override
						public boolean canCreate(Client arg0, String arg1, Message arg2) {
							// TODO Auto-generated method stub
							return true;
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}


		@Override
		public void stopServer() throws RemoteException {
			if (isRunning_){
				try{
					server_.stop();
					server_.join();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		@Override
		public void pushMessages(List<SmsPush> messages) throws RemoteException {
			pushMessage(messages);
		}

	};


	@Override
	public IBinder onBind(Intent arg0) {
		return binder_;
	}

	@Override
	public void onCreate(){
		Log.v(TAG, "Creating WebTextService");
		server_ = new Server(port_);

//		AssetManager am = getAssets();
//		try{
//			File outdir = new File("/sdcard/webtext");
//			if (!outdir.exists())
//				outdir.mkdirs();
//
//			File outFile = new File("/sdcard/webtext/webtext.war");
//
//			if (outFile.exists())
//				outFile.delete();
//			//This is just a sanity check
//
//			if (!outFile.exists()){
//
//				outFile.createNewFile();
//				InputStream in = am.open("webtext.war");
//				OutputStream out = new FileOutputStream(outFile);
//				byte[] buffer = new byte[1024];
//				int count = in.read(buffer);
//				while(count != -1){
//					out.write(buffer, 0, count);
//					count = in.read(buffer);
//				}
//				in.close();
//				out.close();	
//			}
//
//
//		}catch (IOException ex){
//			Log.e(TAG, "Could not create web files.");
//			ex.printStackTrace();
//		}

		server_ = new Server(port_);
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		server_.setHandler(contexts);
		Context webtext = new Context(contexts, "/webtext");
		webtext.addServlet(new ServletHolder(TextServlet.class), "/*");
		webtext.setAttribute(WebText.CONTENT_RESOLVER_ATTRIBUTE, getContentResolver());

		Context pushContext = new Context(contexts, "/cometd");
		ServletHolder cometd_holder = new ServletHolder(comet_servlet_);
		cometd_holder.setInitParameter("timeout","20000");
        cometd_holder.setInitParameter("interval","100");
        cometd_holder.setInitParameter("maxInterval","10000");
        cometd_holder.setInitParameter("multiFrameInterval","5000");
        cometd_holder.setInitOrder(2);
		pushContext.addServlet(cometd_holder, "/*");
		pushContext.addEventListener(new ServletContextAttributeListener(){

			
			@Override
			public void attributeAdded(ServletContextAttributeEvent arg0) {
				if(arg0.getName().equals(Bayeux.DOJOX_COMETD_BAYEUX));
					//PushService.launch((Bayeux)arg0.getValue());				
			}

			@Override
			public void attributeRemoved(ServletContextAttributeEvent arg0) {
			}

			@Override
			public void attributeReplaced(ServletContextAttributeEvent event) {
				if (Bayeux.DOJOX_COMETD_BAYEUX.equals(event.getName()));
					//PushService.launch((Bayeux)event.getValue());
			}

		});
		
		

		
		WebAppContext webapp = new WebAppContext();
		webapp.setWar("/sdcard/webtext/webtext.war");
		webapp.setContextPath("/");
		contexts.addHandler(webapp);

		registerReceiver(receiver_, new IntentFilter(ACTION_RECEIVE));
		registerReceiver(receiver_, new IntentFilter(ACTION_SEND));
	}


	public void pushMessage(List<SmsPush> messages){
		Bayeux b = comet_servlet_.getBayeux();
		if (b == null)
			return;

		Channel channel = b.getChannel("/webtext/push", true);
		
		Client client = b.newClient("client");

		for(SmsPush m: messages){
			Map<String, Object> map = new HashMap<String, Object>();
			StringBuilder builder = new StringBuilder();
			builder.append("<message>");
			builder.append("<addr>");
			builder.append(m.getAddress());
			builder.append("</addr><time>");
			builder.append(m.getTime());
			builder.append("</time><msg_body>");
			builder.append(m.getBody());
			builder.append("</msg_body></message>");

			map.put("payload", builder.toString());
			Log.d(TAG, "Publishing message to " + channel.getSubscribers().size() + " subscribers");
			channel.publish(client, map, "messageId");
		}


	}





}