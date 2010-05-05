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

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webtext.android.services.VerificationService;
import org.webtext.android.services.bindings.IVerificationService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class PairingServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean bound_ = false;
	IVerificationService binder_;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp){
		doPost(req, resp);
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp){
		
		Context context = (Context)getServletContext().getAttribute(WebText.CONTEXT_ATTRIBUTE);
		Intent intent = new Intent(context, VerificationService.class);
		context.bindService(intent, connection_, Context.BIND_AUTO_CREATE);
		while(!bound_);
		try {
			binder_.addIPAddress(req.getRemoteAddr());
			
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				resp.getWriter().write("Could not add IP address");
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
			
		}
		
		try {
			resp.getWriter().write(
					"IP address successfully paired, you may now access your text messages");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		context.unbindService(connection_);
		
	}
	
	private ServiceConnection connection_ = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			binder_ = IVerificationService.Stub.asInterface(arg1);
			bound_ = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			bound_ = false;
		}
		
	};
	

}
