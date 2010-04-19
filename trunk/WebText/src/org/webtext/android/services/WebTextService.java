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
import java.util.List;

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
	
	

	private final IWebTextService.Stub binder_ = new IWebTextService.Stub(){


		@Override
		public void startServer() throws RemoteException {
			if (!isRunning_){
				try {
					server_.start();
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
		}

	};


	@Override
	public IBinder onBind(Intent arg0) {
		return binder_;
	}

	@Override
	public void onCreate(){
		server_ = new Server(port_);
		
		AssetManager am = getAssets();
		try{
			File outdir = new File("/sdcard/webtext");
			if (!outdir.exists())
				outdir.mkdirs();
			
			File outFile = new File("/sdcard/webtext/webtext.war");
			
			if (outFile.exists())
				outFile.delete();
			//This is just a sanity check

			if (!outFile.exists()){
				
				outFile.createNewFile();
				InputStream in = am.open("webtext.war");
				OutputStream out = new FileOutputStream(outFile);
				byte[] buffer = new byte[1024];
				int count = in.read(buffer);
				while(count != -1){
					out.write(buffer, 0, count);
					count = in.read(buffer);
				}
				in.close();
				out.close();	
			}
			
			
		}catch (IOException ex){
			Log.e(TAG, "Could not create web files.");
			ex.printStackTrace();
		}

		server_ = new Server(port_);
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		server_.setHandler(contexts);
		Context webtext = new Context(contexts, "/webtext");
		webtext.addServlet(new ServletHolder(TextServlet.class), "/*");
		webtext.setAttribute(WebText.CONTENT_RESOLVER_ATTRIBUTE, getContentResolver());
		
		Context pushContext = new Context(contexts, "/cometd");
		pushContext.addServlet(new ServletHolder(ContinuationCometdServlet.class), "/*");

		WebAppContext webapp = new WebAppContext();
		webapp.setWar("/sdcard/webtext/webtext.war");
		webapp.setContextPath("/");
		contexts.addHandler(webapp);
	}
	
	public static class SMSPushBroadcastReceiver extends BroadcastReceiver {

				private static final String TAG = "SMSPushBroadcastReceiver";
				
				private static final String ACTION_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";
				private static final String ACTION_SEND = "android.provider.Telephone.SMS_SENT";

				public SMSPushBroadcastReceiver(){
					
				}
				
				@Override
				public void onReceive(android.content.Context arg0, Intent arg1) {
					if (arg1.getAction().equals(ACTION_RECEIVE) || arg1.getAction().equals(ACTION_SEND)){
						Bundle bundle = arg1.getExtras();
						Log.v(TAG, "Received broadcast intent: " + arg1.getAction());
						
						Object[] pdus = (Object[]) bundle.get("pdus");
						List<SmsPush> msgs = new ArrayList<SmsPush>();
						
						for (int i = 0; i < pdus.length; ++i){
							msgs.add(new SmsPush(SmsMessage.createFromPdu((byte[]) pdus[i])));
						}
						
					}

				}

				
				
			}



}
