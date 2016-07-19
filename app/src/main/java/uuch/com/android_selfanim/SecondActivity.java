package uuch.com.android_selfanim;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SecondActivity extends AppCompatActivity {

    private ImageView imageSource = null;
    private ImageView imageTemp = null;
    private SelfDrawView imageDirsction = null;
    private Button button1 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initView();

        initListener();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        imageSource = (ImageView) findViewById(R.id.image_source);
        imageTemp = (ImageView) findViewById(R.id.image_temp);
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
                Bitmap paintBm = CommenUtils.getRatioBitmap(SecondActivity.this, R.drawable.paint, 10, 20);
                imageDirsction.setPaintBm(paintBm);

                imageSource.buildDrawingCache();
                Bitmap bitmapSource = imageSource.getDrawingCache();
                //返回的是处理过的Bitmap
                Bitmap sobelBm = SobelUtils.Sobel(bitmapSource);
                imageTemp.setImageBitmap(sobelBm);

                imageDirsction.beginDraw(getIsNeedFlush(sobelBm));
            }
        });

    }

    private boolean[][] getIsNeedFlush(Bitmap bitmap) {
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
    };
}
