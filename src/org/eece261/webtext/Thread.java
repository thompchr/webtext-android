 /**************************************************************************
 * Copyright 2009 Chris Thompson                                           *
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
package org.eece261.webtext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Thread {
	
	public enum Order{
		Ascending,
		Descending
	}
	
	private int id_;
	private ArrayList<SMS> thread_ = new ArrayList<SMS>();
	private String contactName_;
	private String address_;
	
	public Thread(int id){
		id_ = id;
	}
	
	public int getId(){
		return id_;
	}
	
	public void setContactName(String name){
		contactName_ = name;
	}
	
	public void setAddress(String addr){
		address_ = addr;
	}
	
	public void addSMS(SMS sms){
		thread_.add(sms);
	}
	
	public void sort(Order order){
		if (order == Order.Ascending)
			Collections.sort(thread_, new AscendingComparator());
		else
			Collections.sort(thread_, new DescendingComparator());
	}
	
	private class DescendingComparator implements Comparator<SMS>{

		@Override
		public int compare(SMS arg0, SMS arg1) {
			if (arg0.getDate() > arg1.getDate())
				return 1;
			else if (arg0.getDate() < arg1.getDate())
				return -1;
			else 
				return 0;
		}
		
	}
	
	private class AscendingComparator implements Comparator<SMS>{
		@Override
		public int compare(SMS arg0, SMS arg1) {
			if (arg0.getDate() > arg1.getDate())
				return -1;
			else if (arg0.getDate() < arg1.getDate())
				return 1;
			else 
				return 0;
		}
	}

}
