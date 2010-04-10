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
package org.webtext.android;

public class SMS implements Comparable<SMS>{
	
	private int id_;
	private String body_;
	private long date_;
	private String direction_;
	
	public SMS (int id, String body, long date, String direction){
		body_ = body;
		id_ = id;
		date_ = date;
		direction_ = direction;
	}
	
	public String getBody(){
		return body_;
	}
	
	public int getId(){
		return id_;
	}
	
	public long getDate(){
		return date_;
	}

	@Override
	public int compareTo(SMS o) {
		if (date_ < o.date_)
			return -1;
		else if (date_ > o.date_)
			return 1;
		else
			return 0;
	}
	

}
