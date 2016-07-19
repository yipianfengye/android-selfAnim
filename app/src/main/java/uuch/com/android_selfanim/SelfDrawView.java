package uuch.com.android_selfanim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by aaron on 16/7/12.
 */

public class SelfDrawView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mSurfaceHolder;
    private Bitmap mTmpBm;
    private Canvas mTmpCanvas;
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private int mSrcBmWidth;
    private int mSrcBmHeight;
    private boolean[][] mArray;
    private int offsetY = 100;

    private Bitmap mPaintBm;
    private Point mLastPoint = new Point(0, 0);

    public SelfDrawView(Context context) {
        super(context);
        init();
    }

    public SelfDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
    }

    //设置画笔图片
    public void setPaintBm(Bitmap paintBm) {
        mPaintBm = paintBm;
    }

    //获取离指定点最近的一个未绘制过的点
    private Point getNearestPoint(Point p) {
        if (p == null) return null;
        //以点p为中心，向外扩大搜索范围，每次搜索的是与p点相距add的正方形
        for (int add = 1; add < mSrcBmWidth && add < mSrcBmHeight; add++) {
            //
            int beginX = (p.x - add) >= 0 ? (p.x - add) : 0;
            int endX = (p.x + add) < mSrcBmWidth ? (p.x + add) : mSrcBmWidth - 1;
            int beginY = (p.y - add) >= 0 ? (p.y - add) : 0;
            int endY = (p.y + add) < mSrcBmHeight ? (p.y + add) : mSrcBmHeight - 1;
            //搜索正方形的上下边
            for (int x = beginX; x <= endX; x++) {
                if (mArray[x][beginY]) {
                    mArray[x][beginY] = false;
                    return new Point(x, beginY);
                }
                if (mArray[x][endY]) {
                    mArray[x][endY] = false;
                    return new Point(x, endY);
                }
            }
            //搜索正方形的左右边
            for (int y = beginY + 1; y <= endY - 1; y++) {
                if (mArray[beginX][y]) {
                    mArray[beginX][beginY] = false;
                    return new Point(beginX, beginY);
                }
                if (mArray[endX][y]) {

                    mArray[endX][y] = false;
                    return new Point(endX, y);
                }
            }
        }

        return null;
    }

    //获取下一个需要绘制的点
    private Point getNextPoint() {
        mLastPoint = getNearestPoint(mLastPoint);
        return mLastPoint;
    }


    /**
     * //绘制
     * return :false 表示绘制完成，true表示还需要继续绘制
     */
    private boolean draw() {

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        //获取count个点后，一次性绘制到bitmap在把bitmap绘制到SurfaceView
        int count = 100;
        Point p = null;
        while (count-- > 0) {
            p = getNextPoint();
            if (p == null) {//如果p为空，说明所有的点已经绘制完成
                return false;
            }
            mTmpCanvas.drawPoint(p.x, p.y + offsetY, mPaint);
        }
        //将bitmap绘制到SurfaceView中
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawBitmap(mTmpBm, 0, 0, mPaint);
        if (p != null)
            canvas.drawBitmap(mPaintBm, p.x, p.y - mPaintBm.getHeight() + offsetY, mPaint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        return true;
    }
    //重画
    public void reDraw(boolean[][] array) {
        if (isDrawing) return;
        mTmpBm = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mTmpCanvas = new Canvas(mTmpBm);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mTmpCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        mLastPoint = new Point(0, 0);
        beginDraw(array);
    }

    private boolean isDrawing = false;

    public void beginDraw(boolean[][] array) {
        if (isDrawing) return;
        this.mArray = array;
        mSrcBmWidth = array.length;
        mSrcBmHeight = array[0].length;

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    isDrawing = true;
                    boolean rs = draw();
                    if (!rs) break;
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isDrawing = false;
            }
        }.start();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        mTmpBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mTmpCanvas = new Canvas(mTmpBm);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mTmpCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        Canvas canvas = holder.lockCanvas();
        canvas.drawBitmap(mTmpBm, 0, 0, mPaint);
        holder.unlockCanvasAndPost(canvas);

        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
