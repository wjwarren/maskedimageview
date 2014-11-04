package nl.ansuz.android.maskedimageview.example;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageView test = (ImageView) findViewById(R.id.iv_outline_test);
            test.setClipToOutline(true);
        }
    }


}
