package org.webtext.android.services.bindings;


interface IVerificationService{

	/**
	  * Verify an IP address.  This method
	  * will update the "last contact" field
	  * in the database and delete any
	  * addresses that have exceeded the maximum
	  * time between contacts.
	  *
	  * @param ip - the address to verify
	  *
	  **/
	boolean verifyIPAddress(in String ip);


	  
	/**
	  * Add an IP address to the database of allowed
	  * IP addresses.  This method will also delete
	  * any IP addresses from the database that have
	  * exceeded the allowed time between activity.
	  *
	  * @param ip - the address to allow
	  *
	  **/
	void addIPAddress(in String ip);

}