package uuch.com.android_selfanim;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by aaron on 16/7/11.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    /** LOG标识 */
    // private static final String TAG = "CameraPreview";

    /** 分辨率 */
    public static final int WIDTH = 480;
    public static final int HEIGHT = 800;

    /** 监听接口 */
    private OnCameraStatusListener listener;

    private SurfaceHolder holder;
    private Camera camera;

    // 创建一个PictureCallback对象，并实现其中的onPictureTaken方法
    private android.hardware.Camera.PictureCallback pictureCallback = new android.hardware.Camera.PictureCallback() {

        // 该方法用于处理拍摄后的照片数据
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            // 停止照片拍摄
            camera.stopPreview();
            camera = null;

            // 调用结束事件
            if (null != listener) {
                listener.onCameraStopped(data);
            }
        }
    };

    // Preview类的构造方法
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获得SurfaceHolder对象
        holder = getHolder();
        // 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
        holder.addCallback(this);
        // 设置SurfaceHolder对象的类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 在surface创建时激发
    public void surfaceCreated(SurfaceHolder holder) {
        // Log.e(TAG, "==surfaceCreated==");
        // 获得Camera对象
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        try {
            // 设置用于显示拍照摄像的SurfaceHolder对象
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            // 释放手机摄像头
            camera.release();
            camera = null;
        }
    }

    // 在surface销毁时激发
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Log.e(TAG, "==surfaceDestroyed==");
        // 释放手机摄像头
        camera.release();
    }

    // 在surface的大小发生改变时激发
    public void surfaceChanged(final SurfaceHolder holder, int format, int w,
                               int h) {
        // Log.e(TAG, "==surfaceChanged==");
        try {
            // 获取照相机参数
            Camera.Parameters parameters = camera.getParameters();
            // 设置照片格式
            parameters.setPictureFormat(PixelFormat.JPEG);
            // 设置预浏尺寸
            parameters.setPreviewSize(WIDTH, HEIGHT);
            // 设置照片分辨率
            parameters.setPictureSize(WIDTH, HEIGHT);
            // 设置照相机参数
            camera.setParameters(parameters);
            // 开始拍照
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
    public void takePicture() {
        // Log.e(TAG, "==takePicture==");
        if (camera != null) {
            // 自动对焦
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (null != listener) {
                        listener.onAutoFocus(success);
                    }
                    // 自动对焦成功后才拍摄
                    if (success) {
                        camera.takePicture(null, null, pictureCallback);
                    }
                }
            });
        }
    }

    // 设置监听事件
    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }

    /**
     * 相机拍照监听接口
     */
    public interface OnCameraStatusListener {

        // 相机拍照结束事件
        void onCameraStopped(byte[] data);

        // 拍摄时自动对焦事件
        void onAutoFocus(boolean success);
    }

}
