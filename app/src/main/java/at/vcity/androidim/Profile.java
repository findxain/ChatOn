package at.vcity.androidim;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import at.vcity.androidim.interfaces.IAppManager;
import at.vcity.androidim.services.IMService;
import at.vcity.androidim.tools.FriendController;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.Profiler;

public class Profile extends Activity {



    int Gallery_Item= Menu.FIRST+1;
    int Camera=Menu.FIRST+2;

    TextView username,email;
    Profiler profiler;

    RoundedImageView roundedImageView;

    private IAppManager imService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            imService = ((IMService.IMBinder)service).getService();
            //imService.addNewFriendRequest()
            FriendInfo[] friends = FriendController.getFriendsInfo(); //imService.getLastRawFriendList();
            username.setText(imService.getUsername());

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                       profiler = imService.getUserprofile();
                        ShowIt(profiler);
                    }
                }).start();






        }
        public void onServiceDisconnected(ComponentName className) {
            imService = null;
            Toast.makeText(Profile.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private void ShowIt(final Profiler profiler) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                roundedImageView.setImageBitmap(profiler.pic);
                email.setText(profiler.email);

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username=(TextView)findViewById(R.id.profile_username);
        email=(TextView)findViewById(R.id.profile_email);
        roundedImageView=(RoundedImageView)findViewById(R.id.profile_imageview);


        registerForContextMenu(roundedImageView);


    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);


        if(v.getId()==R.id.profile_imageview)
        {

            menu.add(0,Gallery_Item,0,"Upload Image from Gallery");
            menu.add(0, Camera, 0, "Camera");

        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int selected=item.getItemId();
        if(selected==Camera)
        {
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 12);


        }else if(selected==Gallery_Item)
        {
            Intent PicImageIntent=new Intent(Intent.ACTION_PICK);
            PicImageIntent.setType("image/*");
            startActivityForResult(PicImageIntent,10);

        }

        return super.onContextItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==Activity.RESULT_OK)
        {
            if(requestCode==12)
            {
                Bitmap b=(Bitmap)data.getExtras().get("data");
                roundedImageView.setImageBitmap(b);
                //bitmapp=b;
                uploadToserver(b);




            }
            else if(requestCode==10)
            {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    roundedImageView.setImageBitmap(yourSelectedImage);
                    uploadToserver(yourSelectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            }

        }

    }

    private void uploadToserver(final Bitmap bitmap) {


        new Thread(new Runnable() {
            String result;
            @Override
            public void run() {

                result=imService.UploadPicOfUser(bitmap);
                if(result!=null || !result.equals(IMService.PROFILE_PIC_UPLOAD_FAILED))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplication(),"Pic Uploaded Successfull",Toast.LENGTH_SHORT).show();
                        }
                    });
                    ShowIt(imService.getUserprofile());

                }else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplication(),"Upload Failed, Retry!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        }).start();






    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService(new Intent(Profile.this, IMService.class), mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }


}
