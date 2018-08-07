package com.virtualkey.app;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.virtualkey.utilities.LoggerHelper;

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-06-28
 * @Date Modified: 2018-06-28
 * @Describe:
 * @param:<T>
 * @FIXME
 */
public class App extends Application {
    private static Context paramContext;
    private static LoggerHelper loggerHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        paramContext = getApplicationContext();
        loggerHelper = LoggerHelper.instance();
    }

    public static Context getContext() {
        return paramContext;
    }

    public static Resources getAppResource() {
        return paramContext.getResources();
    }

    public static int getResColor(int id) {
        return paramContext.getResources().getColor(id);
    }

    public static String getResStr(int resId) {
        return paramContext.getString(resId);
    }

    public static String getResStr(int resId, Object... formatArgs) {
        return paramContext.getString(resId, formatArgs);
    }

    public static LoggerHelper getLoggerHelper() {
        if (null == loggerHelper) {
            loggerHelper = LoggerHelper.instance();
        }
        return loggerHelper;
    }

    public static void showAlertDialog(Context context, String msg) {
        new AlertDialog.Builder(context)
                .setTitle("Alert")
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}