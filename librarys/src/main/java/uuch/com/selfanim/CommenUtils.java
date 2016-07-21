package uuch.com.selfanim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by aaron on 16/7/11.
 */

public class CommenUtils {
    /**
     * 转化成灰度图
     * @param bmpOriginal
     * @return
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {

        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Bitmap压缩
     *
     * @param bm
     * @return
     */
    public static Bitmap compress(final Bitmap bm, int reqWidth, int reqHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (height > reqHeight || width > reqWidth) {
            float scaleWidth = (float) reqWidth / width;
            float scaleHeight = (float) reqHeight / height;
            float scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
            bm.recycle();
            return result;
        }
        return bm;
    }


    public static Bitmap getRatioBitmap(Context context, int imgId, int reqWidth, int reqHeight) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), imgId, newOpts);
        newOpts.inSampleSize = calculateInSampleSize(newOpts, reqWidth, reqHeight);

        newOpts.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), imgId, newOpts);

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize >= reqHeight) && (halfWidth / inSampleSize >= reqWidth)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
