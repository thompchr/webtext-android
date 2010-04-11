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

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

public class WebText extends Activity {

	private static final String TAG = "WebText";

	private Server server_;
	private int port_ = 8080;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		AssetManager am = getAssets();
		try{
			File outFile = new File("/sdcard/webtext/webtext.war");
			if (!outFile.exists()){
				outFile.mkdirs();
				outFile.createNewFile();
				InputStream in = am.open("webtext.war");
				OutputStream out = new FileOutputStream(outFile);
				byte[] buffer = new byte[1024];
				int count = in.read(buffer);
				while(count != -1){
					out.write(buffer);
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
		Context webtext = new Context(server_, "/webtext");
		webtext.addServlet(TextServlet.class, "/*");

		WebAppContext webapp = new WebAppContext("/", "/sdcard/webtext/webtext.war");
		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[] { webapp } );
		
		
		try {
			server_.start();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
