package at.vcity.androidim.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.vcity.androidim.types.MessageInfo;
import at.vcity.androidim.types.STATUS;

public class LocalStorageHandler extends SQLiteOpenHelper {

	private static final String TAG = LocalStorageHandler.class.getSimpleName();
	public static final String MESSAGE_TYPE_TEXT="text";
	public static final String MESSAGE_TYPE_PIC="pic";
	public static final String MESSAGE_TYPE_FILE="text";
	public static final String MESSAGE_TYPE_AUDIO="audio";
	public static final String MESSAGE_TYPE_VIDEO="video";

	public static final String LOCALSTORAGE="/sdcard/android-im/";



	public static final String DOWNLOADED="1";
	public static final String NotDOWNLOADED="0";

	Context context;

	private static final String DATABASE_NAME = "AndroidIM.db";
	private static final int DATABASE_VERSION = 1;


	private static final String _ID = "_id";
	private static final String TABLE_NAME_MESSAGES = "androidim_messages";
	public static final String MESSAGE_RECEIVER = "receiver";
	public static final String MESSAGE_SENDER = "sender";
	private static final String MESSAGE_MESSAGE = "message";
	private static final String MESSAGE_TYPE = "type";
	private static final String FILE = "file";
	private static final String DOWNLOAD_STATUS ="download_status" ;



	private static final String TABLE_MESSAGE_CREATE
	= "CREATE TABLE " + TABLE_NAME_MESSAGES
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ MESSAGE_RECEIVER + " VARCHAR(25), "
	+ MESSAGE_SENDER + " VARCHAR(25), "
	+MESSAGE_MESSAGE + " VARCHAR(255)," +
			MESSAGE_TYPE+" VARCHAR(255)," +
			FILE+" VARCHAR(255), "+
			DOWNLOAD_STATUS+ " VARCHAR(255));";
	
	private static final String TABLE_MESSAGE_DROP = 
			"DROP TABLE IF EXISTS "
			+ TABLE_NAME_MESSAGES;
	
	
	public LocalStorageHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_MESSAGE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade der DB von V: "+ oldVersion + " zu V:" + newVersion + "; Alle Daten werden gelscht!");
		db.execSQL(TABLE_MESSAGE_DROP);
		onCreate(db);
		
	}
	
	public void insert(String sender, String receiver, String message,String type,String location,String downloadstatus){
		long rowId = -1;
		try{
			
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(MESSAGE_RECEIVER, receiver);
			values.put(MESSAGE_SENDER, sender);
			values.put(MESSAGE_MESSAGE, message);
			values.put(MESSAGE_TYPE,type);
			values.put(FILE,location);
			values.put(DOWNLOAD_STATUS,downloadstatus);
			rowId = db.insert(TABLE_NAME_MESSAGES, null, values);
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		
	}
	
	public Cursor get(String sender, String receiver)throws Exception{
					

		SQLiteDatabase db = getWritableDatabase();
			String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + _ID + " ASC";
			return db.rawQuery(SELECT_QUERY,null);
			
			//return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
		
	}

	public Cursor getIDnfo(String ID)
	{
		SQLiteDatabase db=getWritableDatabase();
		String Sql_QUERY="select * from "+TABLE_NAME_MESSAGES+" where "+_ID+"='"+ID+"';";

		return db.rawQuery(Sql_QUERY,null);
	}
	public boolean UpdateDownloadStatus(String id,String LocalPath)
	{
		SQLiteDatabase db=getWritableDatabase();
		String where=_ID+"="+id;

		Log.d(TAG,"UpDATe");
		Log.d(TAG,id);

		Log.d(TAG,LocalPath);
		ContentValues content=new ContentValues();
		content.put(DOWNLOAD_STATUS,DOWNLOADED);
		content.put(FILE,LocalPath);

		try
		{
			db.update(TABLE_NAME_MESSAGES,content,where,null);

			Log.d(TAG, "UpDATe Done");

		}catch (Exception e)
		{
			e.printStackTrace();

		}

		return true;
	}



	public static String SavePc(Bitmap imageBitmap) {
		File sdcard=new File("/sdcard/pic/");
		if(sdcard.mkdir())
		{
			Log.d("DIRECTORY CREATED","yess");
		}

		String filename="";
		long millis=System.currentTimeMillis();
		String datetime=new Date().toGMTString();
		datetime=datetime.replace(" ", "");
		datetime=datetime.replace(":", "");


		File f=new File(sdcard,datetime+".png");
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);

			imageBitmap.compress(Bitmap.CompressFormat.PNG, 85, out);
			out.flush();
			out.close();

			Log.d(f.getAbsolutePath().toString(),f.getPath().toString());
			//Uri.parse(sdcard+"/"+requestCode+".png")

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}catch (NullPointerException e)
		{
			e.printStackTrace();
			return "";
		}


		return f.getAbsolutePath().toString();
	}







	

}
