package uuch.com.android_selfanim;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity  implements
        CameraPreview.OnCameraStatusListener{
    public static final int TAKE_PHOTO = 1;
    public static final int SAVE_PHOTO_OK = 2;
    public static final int SAVE_PHOTO_ERROR = 3;

    private CameraPreview mCameraPreview;
    private boolean isTaking = false; // 拍照中

    private ImageView imgView;
    private Button mTakePhotoBtn;
    private Button mSavePhotoBtn;

    private ProgressDialog progressDialog;

    private Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mCameraPreview = (CameraPreview) findViewById(R.id.preview);
        mCameraPreview.setOnCameraStatusListener(this);

        imgView = (ImageView) findViewById(R.id.imgView);
        mTakePhotoBtn = (Button) findViewById(R.id.take_photo_btn);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isTaking = false;
                mCameraPreview.setVisibility(View.VISIBLE);
                (findViewById(R.id.imgPreView_ll)).setVisibility(View.GONE);
            }
        });
        mSavePhotoBtn = (Button) findViewById(R.id.save_photo_btn);
        mSavePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(MainActivity.this,
                        "请稍后", "正在处理");
                progressDialog.setCancelable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        imgView.setDrawingCacheEnabled(true);
                        try {
                            saveImage(imgView.getDrawingCache());
                        } catch (IOException e) {
                            e.printStackTrace();
                            UIHandler.sendEmptyMessage(SAVE_PHOTO_ERROR);
                        }
                        imgView.setDrawingCacheEnabled(false);
                    }
                }).start();
                UIHandler.sendEmptyMessage(SAVE_PHOTO_OK);
            }
        });

        UIHandler = getUIHandler();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*if (event.getAction() == MotionEvent.ACTION_DOWN && !isTaking) {
            isTaking = true;
            mCameraPreview.takePicture();
        }*/
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onCameraStopped(byte[] data) {
        progressDialog = ProgressDialog
                .show(MainActivity.this, "请稍后", "正在处理");
        progressDialog.setCancelable(false);
        mCameraPreview.setVisibility(View.GONE);
        (findViewById(R.id.imgPreView_ll)).setVisibility(View.VISIBLE);

        final Bitmap temp = BitmapFactory.decodeByteArray(data, 0, data.length);
        final Bitmap photo = rotateMap(temp, 90);

        if (photo != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap res;
                    res = SobelUtils.Sobel(photo);
                    photo.recycle();
                    Message msg = new Message();
                    msg.what = TAKE_PHOTO;
                    msg.obj = res;
                    UIHandler.sendMessage(msg);
                }
            }).start();
        }
    }

    @Override
    public void onAutoFocus(boolean success) {
        if (!success) {
            Toast.makeText(MainActivity.this, "对焦失败", Toast.LENGTH_LONG);
        }
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @param rotation
     * @return
     */
    public Bitmap rotateMap(Bitmap bitmap, float rotation) {
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(rotation);
        Bitmap temp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), rotationMatrix, true);
        bitmap.recycle();
        return temp;
    }

    @SuppressLint({ "HandlerLeak", "ShowToast" })
    public Handler getUIHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TAKE_PHOTO:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        imgView.setImageBitmap((Bitmap) msg.obj);
                        break;
                    case SAVE_PHOTO_OK:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(MainActivity.this, "保存成功",
                                Toast.LENGTH_LONG);
                        break;
                    case SAVE_PHOTO_ERROR:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(MainActivity.this, "保存失败",
                                Toast.LENGTH_LONG);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @throws IOException
     */
    public void saveImage(Bitmap bitmap) throws IOException {
        OutputStream out = null;
        if (bitmap != null) {

            long date = System.currentTimeMillis();

            String root = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            File directory = new File(root + File.separator + "GraphicLib");
            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = DateFormat.format("yyyy-MM-dd", date).toString()
                    + ".jpg";
            File file = new File(directory, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
            out.close();
        }
    }

}
