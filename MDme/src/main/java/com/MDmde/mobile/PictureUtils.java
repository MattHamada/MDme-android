package com.MDmde.mobile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

/**
 * Created by Matt Hamada on 4/8/14.
 */

//scales image
public class PictureUtils
{
    /**
     * get a BitmapDrawable from a local file that is scaled down
     * to fit the current window size
     */

    @SuppressWarnings("deprication")
    public static BitmapDrawable getScaledDrawable(Activity a, String path)
    {
        Display display = a.getWindowManager().getDefaultDisplay();
        float destWidth = display.getWidth();
        float destHeight = display.getHeight();

        //get image dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth)
        {
            if (srcWidth > srcHeight)
            {
                inSampleSize = Math.round(srcHeight / destHeight);
            }
            else
            {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(a.getResources(), bitmap);
    }

    public static void cleanImageView(ImageView imageView)
    {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {return;}

        //clean view image to free memory
        BitmapDrawable b = (BitmapDrawable)imageView.getDrawable();
        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

    public static BitmapDrawable getPortraitDrawable(ImageView imageView, BitmapDrawable origImage)
    {
        Matrix m = new Matrix();
        m.postRotate(90);
        Bitmap br = Bitmap.createBitmap(origImage.getBitmap(), 0, 0,
                origImage.getIntrinsicWidth(), origImage.getIntrinsicHeight(), m, true);
        origImage = new BitmapDrawable(imageView.getResources(), br);
        return origImage;
    }

}
