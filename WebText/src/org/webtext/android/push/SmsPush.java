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

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsMessage;

public class SmsPush implements Parcelable {
	
	private String body_;
	private long timestamp_;
	private String displayAddress_;
	
	public static final Parcelable.Creator<SmsPush> CREATOR = new Parcelable.Creator<SmsPush>() {

		@Override
		public SmsPush createFromParcel(Parcel source) {
			return new SmsPush(source);
		}

		@Override
		public SmsPush[] newArray(int size) {
			return new SmsPush[size];
		}
		
	};
	
	public SmsPush(SmsMessage message){
		body_ = message.getDisplayMessageBody();
		timestamp_ = message.getTimestampMillis();
		displayAddress_ = message.getDisplayOriginatingAddress();
	}
	
	public SmsPush(String body, String address, long time){
		body_ = body;
		displayAddress_ = address;
		timestamp_ = time;
	}
	
	public SmsPush(Parcel in){
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(body_);
		out.writeString(displayAddress_);
		out.writeLong(timestamp_);
	}
	
	public void readFromParcel(Parcel in){
		body_ = in.readString();
		displayAddress_ = in.readString();
		timestamp_ = in.readLong();
	}
	
	public String getAddress(){
		return displayAddress_;
	}
	
	public String getBody(){
		return body_;
	}
	
	public long getTime(){
		return timestamp_;
	}
	

}
