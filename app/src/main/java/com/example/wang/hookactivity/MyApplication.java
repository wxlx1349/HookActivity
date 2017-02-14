package com.example.wang.hookactivity;

import com.example.wang.utils.HookAmsUtil;

/**
 * Created by wangxi on 2017/2/14.
 * <p>
 * description :
 * version code: 1
 * create time : 2017/2/14
 * update time : 2017/2/14
 * last modify : wangxi
 */

public class MyApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HookAmsUtil hookAmsUtil = new HookAmsUtil(ProxyActivity.class, this);
        try {
            hookAmsUtil.hookAms();
            hookAmsUtil.hookSystemHandler();
        } catch (Exception e) {

        }
    }
}
