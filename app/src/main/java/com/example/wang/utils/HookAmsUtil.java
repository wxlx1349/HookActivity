package com.example.wang.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by wangxi on 2017/2/14.
 * <p>
 * description :
 * version code: 1
 * create time : 2017/2/14
 * update time : 2017/2/14
 * last modify : wangxi
 */

public class HookAmsUtil {
    private Class<?> proxyActivity;
    private Context context;
    private Object activityThreadObject;   //系统的对象
    private static int LAUNCH_ACTIVITY;

    public HookAmsUtil(Class<?> proxyActivity, Context context) {
        this.proxyActivity = proxyActivity;
        this.context = context;
    }

    public void hookAms() throws Exception {
        Log.i("INFO", "start hook");
        Class<?> forName = Class.forName("android.app.ActivityManagerNative");
        Field defaultField = forName.getDeclaredField("gDefault");
        defaultField.setAccessible(true);
        //gDefault的变量值
        Object defaultValue = defaultField.get(null);
        //反射SingleTon
        Class<?> forName2 = Class.forName("android.util.Singleton");
        Field instanceField = forName2.getDeclaredField("mInstance");
        instanceField.setAccessible(true);
        //系统的iActivityManager
        Object iActivityManagerObject = instanceField.get(defaultValue);
        //钩子
        Class<?> iActivityManagerIntercept = Class.forName("android.app.IActivityManager");
        AmsInvocationHandler handler = new AmsInvocationHandler(iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{iActivityManagerIntercept}, handler);

        instanceField.set(defaultValue, proxy);
        Log.e("INFO", "end hook");
    }

    public void hookSystemHandler() {
        try {
            Log.e("tag2", "changeCallBack");
            Class<?> forName = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = forName.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
            activityThreadObject = currentActivityThreadField.get(null);
            Field handlerField = forName.getDeclaredField("mH");
            handlerField.setAccessible(true);
            //mH的对象
            Class<?> forName2 = Class.forName("android.app.ActivityThread$H");
            Field launchField = forName2.getDeclaredField("LAUNCH_ACTIVITY");
            launchField.setAccessible(true);
            LAUNCH_ACTIVITY = (int) launchField.get(null);

            Handler handler = (Handler) handlerField.get(activityThreadObject);
            Field callbackField = Handler.class.getDeclaredField("mCallback");
            callbackField.setAccessible(true);
            callbackField.set(handler, new ActivityThreadHandlerCallback(handler));
            Log.e("tag2", "changeCallBack2");
        } catch (Exception e) {
            Log.e("tag2", "e");
        }
    }

    class AmsInvocationHandler implements InvocationHandler {
        private Object iActivityManagerObject;

        public AmsInvocationHandler(Object iActivityManagerObject) {
            this.iActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            if ("startActivity".contains(method.getName())) {
                Log.e("AmsInvocationHandler", "tag2--methodName=" + method.getName());
                //偷天换日
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        //说明找到startActivity的方法的intent参数
                        intent = (Intent) args[i];//原意图，过不了安检
                        index = i;
                        break;
                    }
                }
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(context, proxyActivity);
                proxyIntent.setComponent(componentName);
                proxyIntent.putExtra("oldIntent", intent);
                args[index] = proxyIntent;
                Log.e("AmsInvocationHandler", "tag2--componentName=" + intent.getComponent().getClassName());
                return method.invoke(iActivityManagerObject, args);
            }
            return method.invoke(iActivityManagerObject, args);
        }
    }

    class ActivityThreadHandlerCallback implements Handler.Callback {

        Handler handler;

        public ActivityThreadHandlerCallback(Handler handler) {
            super();
            this.handler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            Log.e("ActivityThread", "tag2--handlerMessage");
            //替换回来之前的Intent
            if (msg.what == LAUNCH_ACTIVITY) {
                Log.e("INFO", "launchActivity");
                handleLaunchActivity(msg);
            }
            handler.handleMessage(msg);
            return true;
        }

        private void handleLaunchActivity(Message msg) {
            Object obj = msg.obj;
            try {
                Field intentField = obj.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent intent = (Intent) intentField.get(obj);  //proxyIntent
                Intent oldIntent = intent.getParcelableExtra("oldIntent");
                if (oldIntent != null) {
                    Log.e("tag2", "name=" + oldIntent.getComponent().getClassName());
                    intent.setComponent(oldIntent.getComponent());
                }
            } catch (Exception e) {

            }
        }
    }

}
