package uuch.com.selfanim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

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

    private Bitmap mPaintBm = null;
    private Point mLastPoint = new Point(0, 0);
    private Point mLPoint = new Point(0, 0);

    public SelfDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * 执行组件的初始化方法
     */
    private void initView() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaintBm = CommenUtils.getRatioBitmap(this.getContext(), R.drawable.paint, 10, 20);
    }

    /**
     * 设置画笔图片
     * @param paintBm
     */
    public void setPaintBm(Bitmap paintBm) {
        mPaintBm = paintBm;
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
        clearPaint();
    }


    // ####################### 开始执行组件的绘制操作 ################################

    private boolean isDrawing = false;

    /**
     * 开始执行绘制操作
     * @param array
     * @param width
     * @param height
     */
    public void beginDrawSketch(boolean[][] array, int width, int height) {
        /**
         * 若当前正在绘制,不再执行
         */
        if (isDrawing) {
            return;
        }
        /**
         * 初始化数据
         */
        this.mArray = array;
        mSrcBmWidth = array.length;
        mSrcBmHeight = array[0].length;
        /**
         * 在子线程中执行组件的绘制操作
         */
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    isDrawing = true;
                    boolean rs = doDraw();
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


    /**
     * 执行具体的绘制操作
     * return :false 表示绘制完成，true表示还需要继续绘制
     */
    private boolean doDraw() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        /**
         * 获取count个点后，一次性绘制到bitmap在把bitmap绘制到SurfaceView
         */
        int count = 100;
        Point p = null;
        while (count-- > 0) {
            p = getNextPoint();
            if (p == null) {//如果p为空，说明所有的点已经绘制完成
                clearPaint();
                return false;
            }
            mTmpCanvas.drawPoint(p.x, p.y, mPaint);
        }
        //将bitmap绘制到SurfaceView中
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawBitmap(mTmpBm, 0, 0, mPaint);
        if (p != null)
            canvas.drawBitmap(mPaintBm, p.x, p.y - mPaintBm.getHeight(), mPaint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        return true;
    }

    /**
     * 清除画笔
     */
    private void clearPaint() {
        if (mLPoint != null) {
            mTmpBm = Bitmap.createBitmap(mPaintBm.getWidth(), mPaintBm.getHeight(), Bitmap.Config.ARGB_8888);
            mTmpCanvas = new Canvas(mTmpBm);
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setStyle(Paint.Style.FILL);
            mTmpCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);
            Canvas canvas = mSurfaceHolder.lockCanvas();
            canvas.drawBitmap(mTmpBm, mLPoint.x, mLPoint.y - mTmpBm.getHeight(), mPaint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    /**
     * 获取下一个需要绘制的点
     * @return
     */
    private Point getNextPoint() {
        mLPoint = mLastPoint;
        mLastPoint = getNearestPoint(mLastPoint);
        return mLastPoint;
    }


    /**
     * 获取离指定点最近的一个未绘制过的点
     * @param p
     * @return
     */
    private Point getNearestPoint(Point p) {
        if (p == null) return null;
        //以点p为中心，向外扩大搜索范围，每次搜索的是与p点相距add的正方形
        for (int add = 1; add < mSrcBmWidth && add < mSrcBmHeight; add++) {
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

    /**
     * 完成对Bitmap的像素点的是否绘制标识标志操作
     * @param bitmap
     * @return
     */
    public static boolean[][] getIsNeedFlush(Bitmap bitmap) {
        boolean[][] b = new boolean[bitmap.getWidth()][bitmap.getHeight()];

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                if (bitmap.getPixel(i, j) != Color.WHITE)
                    b[i][j] = true;
                else
                    b[i][j] = false;
            }
        }
        return b;
    }
}
