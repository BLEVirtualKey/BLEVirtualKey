package com.virtualkey.app.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.virtualkey.app.App;

/**
 * Toast工具类
 *
 * @author GaiQS E-mail:gaiqs@sina.com
 * @Date: 2015年1月28日
 * @Time: 下午2:54:05
 */
public class ToastUtils {

    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    public static void showToast(Context mContext, String text) {
        showToast(mContext, text, Toast.LENGTH_LONG);
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }

    private static void showToast(Context mContext, String text, int duration) {
        mHandler.removeCallbacks(r);
        if (mToast != null) {
            mToast.setText(text);
            mToast.show();
        } else {
            mToast = Toast.makeText(mContext, text, duration);
            mHandler.postDelayed(r, duration);
            mToast.show();
        }
    }

    public static void toastShort(String text) {
        Toast.makeText(App.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void toastShort(int resId) {
        Toast.makeText(App.getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String text) {
        Toast.makeText(App.getContext(), text, Toast.LENGTH_LONG).show();
    }

    public static void toastLong(int resId) {
        Toast.makeText(App.getContext(), resId, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(int resId, Object... params) {
        Context context = App.getContext();
        if (null != params) {
            Toast.makeText(context, context.getString(resId, params), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
        }
    }

    public static long LAST_CLOCK_TIME;

    // 防误点
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - LAST_CLOCK_TIME < 1200) {
            return true;
        }
        LAST_CLOCK_TIME = time;
        return false;
    }

}
