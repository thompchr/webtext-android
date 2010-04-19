
package org.webtext.android.services.bindings;

import org.webtext.android.push.SmsPush;

interface IWebTextService {

	void startServer();
	
	void stopServer();
	
	void pushMessages(in List<SmsPush> messages);
}
	