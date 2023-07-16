package com.mobiletest.awesomeperformance.util;


import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.util.Log;


public class HookUtils {

    private static final String TAG = "HookUtils";

    public static void hookOnClick(View view) {
        View.OnClickListener originalClickListener = getOnClickListener(view);

        if (originalClickListener != null) {
            View.OnClickListener hookedClickListener = (View.OnClickListener) Proxy.newProxyInstance(
                    originalClickListener.getClass().getClassLoader(),
                    new Class[]{View.OnClickListener.class},
                    new ClickInvocationHandler(originalClickListener));

            setOnClickListener(view, hookedClickListener);
        }
    }

    private static View.OnClickListener getOnClickListener(View view) {
        try {
            Method getListenerInfoMethod = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfoMethod.setAccessible(true);
            Object listenerInfoObj = getListenerInfoMethod.invoke(view);

            Class<?> listenerInfoClz = Class.forName("android.view.View$ListenerInfo");
            Field onClickListenerField = listenerInfoClz.getDeclaredField("mOnClickListener");
            onClickListenerField.setAccessible(true);
            return (View.OnClickListener) onClickListenerField.get(listenerInfoObj);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get OnClickListener", e);
        }
        return null;
    }

    private static void setOnClickListener(View view, View.OnClickListener listener) {
        try {
            Method setOnClickListenerMethod = View.class.getDeclaredMethod("setOnClickListener", View.OnClickListener.class);
            setOnClickListenerMethod.setAccessible(true);
            setOnClickListenerMethod.invoke(view, listener);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set OnClickListener", e);
        }
    }
}

class ClickInvocationHandler implements InvocationHandler {

    private View.OnClickListener originalClickListener;

    public ClickInvocationHandler(View.OnClickListener originalClickListener) {
        this.originalClickListener = originalClickListener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("onClick")) {
            long timestamp = System.currentTimeMillis();
            Log.d("HookUtils", "Timestamp: " + timestamp);
        }

        if (originalClickListener != null) {
            return method.invoke(originalClickListener, args);
        }

        return null;
    }
}
