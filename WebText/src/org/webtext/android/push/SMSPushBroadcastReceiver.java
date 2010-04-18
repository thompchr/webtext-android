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
package org.webtext.android.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSPushBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "SMSPushBroadcastReceiver";
	
	private static final String ACTION_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTION_SEND = "android.provider.Telephone.SMS_SENT";
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if (arg1.getAction().equals(ACTION_RECEIVE) || arg1.getAction().equals(ACTION_SEND)){
			Bundle bundle = arg1.getExtras();
			Log.v(TAG, "Received broadcast intent: " + arg1.getAction());
			
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; ++i){
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			
			
			
		}

	}
	
	


}
