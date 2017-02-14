package com.example.wang.hookactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by wangxi on 2017/2/14.
 * <p>
 * description :
 * version code: 1
 * create time : 2017/2/14
 * update time : 2017/2/14
 * last modify : wangxi
 */

public class OtherActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
    }

    public void toActivity(View view) {
        startActivity(new Intent(this, ThirdActivity.class));
    }
}
