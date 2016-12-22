package at.vcity.androidim.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Lap Mania on 3/12/2016.
 */
public class ImageHandlet {

    public ImageHandlet()
    {

    }


    public static String BitmapTOString(Bitmap bitmap) {
        // TODO Auto-generated method stub
        Bitmap bm =bitmap;
        String imgString="";
        try {


            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 70, stream);
            byte[] byteFormat = stream.toByteArray();

            imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        }catch (Exception e)
        {
            e.printStackTrace();

        }
        Log.d("Bitmap", imgString);

        return imgString;
    }


    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap GetBitmapFromPath(String picPath) {


        Log.d("GetImageBitmap",picPath+"");
        Bitmap bitmap=null;
        File f= new File(picPath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }


        return bitmap;
    }


}
