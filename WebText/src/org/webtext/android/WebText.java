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

import org.webtext.android.services.WebTextService;
import org.webtext.android.services.bindings.IWebTextService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WebText extends Activity {

	private static final String TAG = "WebText";
	public static final String CONTENT_RESOLVER_ATTRIBUTE = "org.webtext.android.contentResolver";
	private IBinder binder_ = null;

	private ServiceConnection webTextServiceConnection_ = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Log.d(TAG, "WebTextService bound to activity");
			binder_ = arg1;
			IWebTextService pipe = IWebTextService.Stub.asInterface(binder_);
			try {
				pipe.startServer();
			} catch (RemoteException e) {

				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			binder_ = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		



	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.v(TAG, "Starting service");

		Intent intent = new Intent(this, WebTextService.class);
		
		bindService(intent, webTextServiceConnection_, BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause(){
		super.onPause();
		unbindService(webTextServiceConnection_);
	}

}
