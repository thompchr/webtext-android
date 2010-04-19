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

import java.util.Map;

import org.cometd.Bayeux;
import org.cometd.Client;
import org.mortbay.cometd.BayeuxService;

public class PushService extends BayeuxService {
	
	private static PushService instance_;
	
	public static PushService launch(Bayeux bayeux){
		if (instance_ == null)
			instance_ = new PushService(bayeux);
		return instance_;
	}

	private PushService(Bayeux bayeux) {
		super(bayeux, "webtext");
		subscribe("/webtext/push", "receiveMsg");
		
	}
	

	public void receiveMsg(Client client, String channel, 
			Map<String, String> data, String messageId){
		String msg = data.get("data").trim();
	}

}
