package uuch.com.android_selfanim;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import uuch.com.selfanim.CommenUtils;
import uuch.com.selfanim.DrawUtils;
import uuch.com.selfanim.SelfDrawView;

/**
 * 绘制操作工具类
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageSource = null;
    private SelfDrawView imageDirsction = null;
    private Button button1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initListener();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        imageSource = (ImageView) findViewById(R.id.image_source);
        imageDirsction = (SelfDrawView) findViewById(R.id.image_direction);
        button1 = (Button) findViewById(R.id.button1);
    }

    /**
     * 初始化事件监听
     */
    private void initListener() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap paintBm = CommenUtils.getRatioBitmap(MainActivity.this, R.drawable.paint, 10, 20);

                imageSource.buildDrawingCache();
                Bitmap bitmapSource = imageSource.getDrawingCache();
                /**
                 * 开始执行绘制素描的操作
                 */
                imageDirsction.setPaintBm(paintBm);
                DrawUtils.startSelfDraw(imageDirsction, bitmapSource);
            }
        });

    }
}
