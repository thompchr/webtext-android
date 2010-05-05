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

package org.webtext.android.services;

import org.webtext.android.services.bindings.IVerificationService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.IBinder;
import android.os.RemoteException;

public class VerificationService extends Service {
	
	private SQLiteDatabase db_;
	private SQLiteStatement insertStatement_;
	private SQLiteStatement cleanStatement_;
	private SQLiteStatement verifyStatement_;
	private SQLiteStatement updateStatement_;
	
	
	private IVerificationService.Stub binder_ = new IVerificationService.Stub(){

		@Override
		public void addIPAddress(String ip) throws RemoteException {
			insertStatement_.bindString(1, ip);
			insertStatement_.bindLong(2, System.currentTimeMillis());
			insertStatement_.executeInsert();
			
			cleanStatement_.bindLong(1, System.currentTimeMillis());
			cleanStatement_.execute();
			
			updateStatement_.bindLong(1, System.currentTimeMillis());
			updateStatement_.bindString(2, ip);
			updateStatement_.execute();			
			
		}

		@Override
		public boolean verifyIPAddress(String ip) throws RemoteException {
			
			verifyStatement_.bindString(1, ip);
			verifyStatement_.bindLong(2, System.currentTimeMillis());
			if(verifyStatement_.simpleQueryForLong() == 1){
				updateStatement_.bindLong(1, System.currentTimeMillis());
				updateStatement_.bindString(2, ip);
				updateStatement_.execute();	
				return true;
			}		
			return false;
		}
		
	};

	@Override
	public IBinder onBind(Intent arg0) {

		return binder_;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		OpenHelper helper = new OpenHelper(this);
		db_ = helper.getWritableDatabase();
		insertStatement_ = db_.compileStatement(OpenHelper.INSERT_STATEMENT);
		cleanStatement_ = db_.compileStatement(OpenHelper.CLEAN_STATEMENT);
		verifyStatement_ = db_.compileStatement(OpenHelper.VERIFY_STATEMENT);
		updateStatement_ = db_.compileStatement(OpenHelper.UPDATE_STATEMENT);
		
		
	}
	
	private class OpenHelper extends SQLiteOpenHelper{
		
		private static final String TABLE_NAME = "addresses";
		
		private static final String DATABASE_CREATION_STRING = "create table " + TABLE_NAME +
				" (id integer primary key,address text, last_access integer)";
		
		private static final String DATABASE_NAME = "verification";
		
		private static final int DATABASE_VERSION = 1;
		
		public static final String INSERT_STATEMENT = "insert into " + TABLE_NAME + 
									" (address,last_access) values(?,?)";
		
		public static final String UPDATE_STATEMENT = "update " + TABLE_NAME + " set " +
				"last_access = ? where address like ?";
		
		private static final long THRESHOLD = 1800000; //30 minutes
		
		public static final String CLEAN_STATEMENT = "delete from " + TABLE_NAME +
									" where (? - last_access) > " + THRESHOLD;
		
		public static final String VERIFY_STATEMENT = "select count(*) from " +
									TABLE_NAME + " where address like ? and " +
											"(? - last_access) > " + THRESHOLD;
		
		

		public OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATION_STRING);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
	}

}
