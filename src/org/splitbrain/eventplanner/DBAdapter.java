package org.splitbrain.eventplanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	private static final String DATABASE_NAME = "EventPlanner";
	private static final int DATABASE_VERSION = 1;

	private final Context context;
	
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public DBAdapter(Context context){
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		DBHelper.close();
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		private final Context context;
		
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		/**
		 * 
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = readAssetFile("db/0.sql");
			try {
				db.execSQL(sql);
			}catch(SQLException e){
				Log.e("db","Failed to execute SQL :"+e.toString());
			}
		}

		/**
		 * 
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			for(int version=oldVersion+1; version<=newVersion; version++){
				String sql = readAssetFile("db/"+version+".sql");
				try {
					db.execSQL(sql);
				}catch(SQLException e){
					Log.e("db","Failed to execute SQL :"+e.toString());
				}
			}
		}
		
	    /**
	     * Load Data from file in assets
		 * 
	     * @return data from file
	     * @fixme change to return array of strings 
	     */
	    private String readAssetFile(String filename) {
	    	AssetManager assetManager = context.getAssets();
			InputStream inputStream = null;
			try{
				inputStream = assetManager.open(filename);
			} catch (IOException e) {
				Log.e("db","Failed to open Asset File. "+e.toString());
				return "";
			}
	    		    	
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        byte buf[] = new byte[1024];
	        int len;
	        try {
	            while ((len = inputStream.read(buf)) != -1) {
	                outputStream.write(buf, 0, len);
	            }
	            outputStream.close();
	            inputStream.close();
	        } catch (IOException e) {
	        	Log.e("db","Failed to read Asset File "+e.toString());
	        }
        	return outputStream.toString();
	    }

	}
}
