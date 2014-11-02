package nl.ansuz.android.maskedimageview.example;

import android.app.Activity;
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

        ImageView test = (ImageView) findViewById(R.id.iv_outline_test);
        test.setClipToOutline(true);
    }


}
