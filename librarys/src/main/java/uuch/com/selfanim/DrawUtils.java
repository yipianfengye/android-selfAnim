package uuch.com.selfanim;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by aaron on 16/7/21.
 * 绘制工具类
 */
public class DrawUtils {

    /**
     * 开始执行组件的绘制操作
     * @param view
     * @param mBitmap
     */
    public static void startSelfDraw(SelfDrawView view, Bitmap mBitmap) {
        if (view == null || mBitmap == null) {
            return;
        }


        /**
         * 返回的是处理过的Bitmap
         */
        Bitmap sobelBm = SobelUtils.Sobel(mBitmap);
        /**
         * 返回boolean 二维数组
         */
        boolean[][] array = SelfDrawView.getIsNeedFlush(sobelBm);
        int width = array.length;
        int height = array[0].length;

        view.beginDrawSketch(array, width, height);
    }


    /**
     * 设置显示Bitmap图片
     * @param view
     * @param mBitmap
     */
    public static void startBitmapDraw(View view, Bitmap mBitmap) {
        if (view == null || mBitmap == null) {
            return;
        }

        Drawable drawable = new BitmapDrawable(mBitmap);
        view.setBackgroundDrawable(drawable);
    }

    /**
     * 更新画笔
     * @param selfDrawView
     * @param mBitmap
     */
    public static void updatePaintBitmap(SelfDrawView selfDrawView, Bitmap mBitmap) {
        if (selfDrawView == null || mBitmap == null) {
            return;
        }

        selfDrawView.setPaintBm(mBitmap);
    }
}
