package com.less.bzrefreshbtn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * @author Administrator
 */
public class TestActivity extends Activity {
    private BZRreshshBtn bzRreshshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bz);
        bzRreshshBtn = findViewById(R.id.btn_refresh);
    }

    public void handle(View view) {
        if (view.getId() == R.id.btn_start) {
            Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
            bzRreshshBtn.start();
        } else if (view.getId() == R.id.btn_stop) {
            Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show();
            bzRreshshBtn.stop();
        }
    }

}
