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

package org.webtext.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mortbay.cometd.continuation.ContinuationCometdServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

public class WebText extends Activity {

	private static final String TAG = "WebText";
	public static final String CONTENT_RESOLVER_ATTRIBUTE = "org.webtext.android.contentResolver";

	private Server server_;
	private int port_ = 8080;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

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
			
			File webdef = new File("/sdcard/webtext/webdefault.xml");
			if (webdef.exists()) { webdef.delete(); }
			
			//This is nothing more than a sanity check.
			if (!webdef.exists()){
				webdef.createNewFile();
				InputStream in = am.open("webdefault.xml");
				OutputStream out = new FileOutputStream(webdef);
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
		webtext.setAttribute(CONTENT_RESOLVER_ATTRIBUTE, getContentResolver());
		
		Context pushContext = new Context(contexts, "/cometd");
		pushContext.addServlet(new ServletHolder(ContinuationCometdServlet.class), "/*");

		WebAppContext webapp = new WebAppContext();
		webapp.setWar("/sdcard/webtext/webtext.war");
		webapp.setContextPath("/");
		contexts.addHandler(webapp);

		try {
			server_.start();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
