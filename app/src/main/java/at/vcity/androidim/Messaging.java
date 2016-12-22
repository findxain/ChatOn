package at.vcity.androidim;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import at.vcity.androidim.communication.SocketOperator;
import at.vcity.androidim.interfaces.IAppManager;
import at.vcity.androidim.services.IMService;
import at.vcity.androidim.tools.FriendController;
import at.vcity.androidim.tools.ImageHandlet;
import at.vcity.androidim.tools.LocalStorageHandler;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.MessageInfo;


public class Messaging extends Activity {



	String picPath="";

	private static int AUDIO=Menu.FIRST+1;
	private static final int CAMERA = AUDIO+1;
	private static final int GALLERY = CAMERA+1;
	private static final int VIDEO =GALLERY+1 ;
	private static final int FILE = VIDEO+1;
	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	//public String username;
	private EditText messageText;

	private Button sendMessageButton;
	private IAppManager imService;
	private FriendInfo friend = new FriendInfo();
	private LocalStorageHandler localstoragehandler; 
	private Cursor dbCursor;
	LinearLayout mEssageBox;
	ScrollView scrollView;
	WindowManager windowManager;
	int Width=0;
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((IMService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(Messaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); //messaging_screen);
		bindService(new Intent(Messaging.this, IMService.class), mConnection, Context.BIND_AUTO_CREATE);
		mEssageBox=(LinearLayout)findViewById(R.id.messageBoxLinearayout);
		scrollView=(ScrollView)findViewById(R.id.messagescroolview);

		
		messageText = (EditText) findViewById(R.id.message);
		
		messageText.requestFocus();			
		
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		
		Bundle extras = this.getIntent().getExtras();
		
		
		friend.userName = extras.getString(FriendInfo.USERNAME);
		friend.ip = extras.getString(FriendInfo.IP);
		friend.port = extras.getString(FriendInfo.PORT);
		String msg = extras.getString(MessageInfo.MESSAGETEXT);

		setTitle("Messaging with " + friend.userName);

		windowManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Width=(display.getWidth()/3)*2;
		
	//	EditText friendUserName = (EditText) findViewById(R.id.friendUserName);
	//	friendUserName.setText(friend.userName);
		
		
		localstoragehandler = new LocalStorageHandler(this);

        try {
            dbCursor = localstoragehandler.get(friend.userName, IMService.USERNAME );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dbCursor.getCount() > 0)
		{
			int noOfScorer = 0;
			dbCursor.moveToFirst();
		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;
				String  msg0=dbCursor.getString(0);
				String  msg2=dbCursor.getString(2);
				String msg3=dbCursor.getString(3);
				String msg4=dbCursor.getString(4);
				String msg5=dbCursor.getString(5);
				String msg6=dbCursor.getString(6);

				Log.d("---------------------","------------------");
				Log.d("DATABASE",msg0);
				Log.d("DATABASE",msg2);
				Log.d("DATABASE",msg3);
				Log.d("DATABASE",msg4+"");
				Log.d("DATABASE",msg5+"");
				Log.d("DATABASE",msg6+"");
				Log.d("--------END-------","-------END-----------");
				//Log.d("While Cuser "+msg2,msg3+msg4);
				this.appendToMessageHistory(msg0,msg2, msg3,msg4,msg5,msg6 );



		        dbCursor.moveToNext();
		    }
		}else
		{
			Log.d("Localdb erorr","else");
		}
		localstoragehandler.close();
		
		if (msg != null) 
		{
			this.appendToMessageHistory("",friend.userName , msg,"",null,"");
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				sendMessage(MessageInfo.MESSAGE_TYPE_TEXT,null);
				
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
		break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(Messaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked OK so do some stuff */
				}
			})        
			.create();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		FriendController.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(Messaging.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(IMService.TAKE_MESSAGE);
		registerReceiver(messageReceiver, i);
		
		FriendController.setActiveFriend(friend.userName);		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			String username = extra.getString(MessageInfo.USERID);			
			String message = extra.getString(MessageInfo.MESSAGETEXT);
			String sendt=extra.getString(MessageInfo.SENDT);
			String messageType=extra.getString(MessageInfo.MESSAGETYPE);
			String messagefile=extra.getString(MessageInfo.FILE);
			Bitmap bitmap;
			picPath ="";
			String downloadstatus="";



			if (username != null && message != null )
			{
				if (friend.userName.equals(username)) {

					if(messageType.equals(MessageInfo.MESSAGE_TYPE_PIC))
					{
						picPath=messagefile;
					}else if(messageType.equals(MessageInfo.MESSAGE_TYPE_VIDEO))
					{
						Log.d("Message Type",MessageInfo.MESSAGE_TYPE_VIDEO);
						picPath=messagefile;
						downloadstatus=LocalStorageHandler.NotDOWNLOADED;

					}

					appendToMessageHistory("",username, message, messageType,picPath,"");
					localstoragehandler.insert(username,imService.getUsername(), message,messageType,picPath,downloadstatus);
					
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(Messaging.this,  username + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void appendToMessageHistory(String id,String uname, String messa,String messageType, final String filepath, String DownloadStatus) {


			//TextView tv=new TextView(Messaging.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Width,LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0,10,0,0);

			View v=getLayoutInflater().inflate(R.layout.message_entity,null);


        	TextView mess;
        	TextView time;
			final ImageView iv;
		final ProgressBar progressBar;
			if(friend.userName.equals(uname))
			{
                v=getLayoutInflater().inflate(R.layout.message_entity,null);
                mess=(TextView)v.findViewById(R.id.message_entity_message);
                time=(TextView)v.findViewById(R.id.message_entity_time);
                iv=(ImageView)v.findViewById(R.id.message_entity_imageview);

				progressBar=(ProgressBar)v.findViewById(R.id.message_entity_progressbar);
			}else
			{
                v=getLayoutInflater().inflate(R.layout.message_entity_right,null);
                lp.gravity=Gravity.RIGHT;
                mess=(TextView)v.findViewById(R.id.message_entity_right_message);
                time=(TextView)v.findViewById(R.id.message_entity_right_time);
                iv=(ImageView)v.findViewById(R.id.message_entity_right_imageview);

				progressBar=(ProgressBar)v.findViewById(R.id.message_entity_Right_progressbar);


			}

		try {
			progressBar.setId(Integer.parseInt(id));

		}catch (Exception e)
		{
			e.printStackTrace();
		}

			if(messageType!=null) {
				if (messageType.equals(MessageInfo.MESSAGE_TYPE_PIC)) {
					iv.setImageBitmap(ImageHandlet.GetBitmapFromPath(filepath));
					progressBar.setVisibility(View.GONE);
					iv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse("file://"+filepath), "image/png");
							startActivity(intent);
							Log.d("FIlePAth",filepath);
						}
					});

				}else if(messageType.equals(MessageInfo.MESSAGE_TYPE_VIDEO))
				{
					if(DownloadStatus.equals(LocalStorageHandler.DOWNLOADED)) {


						Bitmap bitTh = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
						iv.setImageBitmap(bitTh);
						progressBar.setVisibility(View.GONE);
						iv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(filepath));
								intent.setDataAndType(Uri.parse(filepath), "video/mp4");
								startActivity(intent);

							}
						});
					}else if(DownloadStatus.equals(LocalStorageHandler.NotDOWNLOADED))
					{
						iv.setTag(id);
						iv.setImageResource(R.drawable.download);
						iv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {


								progressBar.setVisibility(View.VISIBLE);
								Cursor c=localstoragehandler.getIDnfo(iv.getTag().toString());
								String filepath="";
								//Toast.makeText(getApplication(),iv.getTag().toString()+" iD " +iv.getId(),Toast.LENGTH_SHORT).show();

								while (c.moveToNext())
								{
									String  msg0=c.getString(0);
									String  msg2=c.getString(2);
									String msg3=c.getString(3);
									String msg4=c.getString(4);
									String msg5=c.getString(5);
									String msg6=c.getString(6);

									filepath=msg5;

									Log.d("-----------Vedio-----","------------------");
									Log.d("DATABASE---------",msg0);
									Log.d("DATABASE-------",msg2);
									Log.d("DATABASE---------",msg3);
									Log.d("DATABASE-----",msg4+"");
									Log.d("DATABASE-------",msg5+"");
									Log.d("DATABASE----------",msg6+"");
									Log.d("--------END-------","-------END-----------");


								}

								Toast.makeText(getApplicationContext(),filepath.toString()+iv.getTag().toString(),Toast.LENGTH_SHORT).show();

								DownloadFileFromURL downloadFileFromURL=new DownloadFileFromURL(filepath,iv.getTag().toString());
								downloadFileFromURL.execute("");



							}
						});


					}
				}else {
					iv.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);
				}
			}

			mess.setText(messa);
			//time.setText(sendt);
			v.setLayoutParams(lp);


			mEssageBox.addView(v);
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.fullScroll(View.FOCUS_DOWN);
				messageText.requestFocus();
			}
		});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, AUDIO, 0, "AUDIO");
		menu.add(0, CAMERA, 0, "Camera");
		menu.add(0, GALLERY, 0, "Gallery");
		menu.add(0, VIDEO, 0, "Video");
		menu.add(0, FILE, 0, "File");


		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		int id=item.getItemId();

		if(id==AUDIO)
		{

		}else if(id==CAMERA)
		{
			startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),50);
		}else if(id==GALLERY)
        {
            Intent PicImageIntent=new Intent(Intent.ACTION_PICK);
            PicImageIntent.setType("image/*");
            startActivityForResult(PicImageIntent,10);
        }else if(id==VIDEO)
		{
			startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE),20);

		}
		else if(id==FILE)
		{
			Intent FileIntent=new Intent(Intent.ACTION_GET_CONTENT);
			FileIntent.setType("*/.pdf");
			FileIntent.addCategory(Intent.CATEGORY_DEFAULT);
			startActivityForResult(FileIntent,30);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK)
		{
			if (requestCode==30)
			{
				Uri uri=data.getData();

				String[] proj={MediaStore.MediaColumns.DATA};
				Log.d("URI",uri.toString());
				Cursor c=getContentResolver().query(uri,proj,"",null,"");

				Log.d("URI",c.getColumnCount()+"");

				Log.d("URI",c.getCount()+"");

				while (c.moveToNext())
				{
					Log.d("URI",c.getString(0)+"");

//					Log.d("URI",c.getString(1));

//					Log.d("URI",c.getString(2));
				}

			}

            if(requestCode==10)
            {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    sendMessage(MessageInfo.MESSAGE_TYPE_PIC,yourSelectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

			else if(requestCode==50)
			{
				Bitmap b= (Bitmap)data.getExtras().get("data");
				//String image=BitmapTOString(b);
				Log.d("Image Captured","YEs");
				sendMessage(MessageInfo.MESSAGE_TYPE_PIC, b);


			}
			else if(requestCode==20)
			{

				Uri uri=data.getData();
				String Absolutepath=getRealPathFromURI(uri);
				sendVideomessage(Absolutepath);



			}

		}

	}

	private void sendVideomessage(final String absolutepath) {

		final String message=messageText.getText().toString();

		localstoragehandler.insert(imService.getUsername(), friend.userName, message, MessageInfo.MESSAGE_TYPE_VIDEO, absolutepath,LocalStorageHandler.DOWNLOADED);
		appendToMessageHistory("", imService.getUsername(), message, MessageInfo.MESSAGE_TYPE_VIDEO, absolutepath, LocalStorageHandler.DOWNLOADED);
		messageText.setText("");

		class UploadVideo extends AsyncTask<Void, Void, String> {
			ProgressDialog uploading;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				uploading = ProgressDialog.show(Messaging.this, "Uploading File", "Please wait...", false, false);
			}
			@Override
			protected String doInBackground(Void... params) {
				return	imService.sendVideoMessage(imService.getUsername(),friend.userName,message,MessageInfo.MESSAGE_TYPE_VIDEO,"",absolutepath);
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);
				uploading.dismiss();
				Log.d("POST Execute",s+"");

				//textViewResponse.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
				//textViewResponse.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
		UploadVideo uv = new UploadVideo();
		uv.execute();
	}


	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}


	private void sendMessage(final String messageType, Bitmap image)
	{
		Log.d("Sending Message",messageType);
		final CharSequence message;
		final Handler handler = new Handler();
		message = messageText.getText();
		picPath="";
		if (message.length()>0)
		{


			if(messageType.equals(MessageInfo.MESSAGE_TYPE_PIC))
			{

					Log.d("Message type", MessageInfo.MESSAGE_TYPE_PIC);
					picPath=localstoragehandler.SavePc(image);

			}
			appendToMessageHistory("",imService.getUsername(), message.toString(),messageType,picPath,"");

			localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString(), messageType, picPath, LocalStorageHandler.DOWNLOADED);

			messageText.setText("");
			Thread thread = new Thread(){
				public void run() {
					try {
						if (imService.sendMessage(imService.getUsername(), friend.userName, message.toString(),messageType+"",picPath+"") == null)
						{

							handler.post(new Runnable(){

								public void run() {

									Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();


									//showDialog(MESSAGE_CANNOT_BE_SENT);
								}

							});
						}
					} catch (UnsupportedEncodingException e) {
						Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

						e.printStackTrace();
					}
				}
			};
			thread.start();

		}
	}
	public String getRealPathFromURI(Uri contentUri) {
		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		if(cursor.moveToFirst())
		{
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}else
		{
			res="";
		}
		cursor.close();
		return res;
	}



	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 * */
		String Address=SocketOperator.AUTHENTICATION_SERVER_ADDRESS;
		String Filename="";
		String dbID="";
		ProgressBar progressBar;


		public DownloadFileFromURL(String fileLOcation,String dbID)
		{
			Address+=fileLOcation;
			//fileLOcation.replace("video/","");
			Filename=fileLOcation.replace("video/","");
			this.dbID=dbID;
			progressBar=(ProgressBar)findViewById(Integer.parseInt(dbID));


		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setIndeterminate(false);
			progressBar.setMax(100);
			progressBar.setProgress(0);
		}



		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(Address);
				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				int lenghtOfFile = conection.getContentLength();

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				// Output stream to write file

				File sdcard=new File(LocalStorageHandler.LOCALSTORAGE+"videos/");
				if(sdcard.mkdirs())
				{
					Log.d("DIRECTORY CREATED","yess");
				}
//				File sdcard_androidim_vedios=new File(sdcard,"videos/");
//				sdcard.mkdi

				File f=new File(sdcard,Filename);




				FileOutputStream fileOutputStream=new FileOutputStream(f);


				OutputStream output = fileOutputStream;

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress(""+(int)((total*100)/lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			progressBar.setProgress(Integer.parseInt(progress[0]));
			Log.d("PRogress", Integer.parseInt(progress[0]) + "");
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {

			//Toast.makeText(getApplicationContext(),"Downloaded",Toast.LENGTH_SHORT).show();
			String newpath=LocalStorageHandler.LOCALSTORAGE+Filename;
			Log.d("postExecute",newpath);
			localstoragehandler.UpdateDownloadStatus(dbID,LocalStorageHandler.LOCALSTORAGE+"videos/"+Filename);
			progressBar.setVisibility(View.INVISIBLE);


		}

	}



}
