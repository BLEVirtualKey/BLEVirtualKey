package com.virtualkey.app.utils;

import android.util.Log;

import java.util.Hashtable;

/**
 * log帮助类
 * <p/>
 * 0.使用一个标签来标记当前的AP
 * 1.支持多用户打印Log（在AP比较大，文件比较多，每人负责的模块不同时，可以使用自己的log来打印，这样看log的时候可以快速筛选出当前AP中你所设置的Log，
 * 比如只看kesen的log就可以在Eclipse的filter里面输入kesen，这样显示的就都是你的log了）
 * 2.显示当前的线程名
 * 3.显示当前的Java文件与打印log的行号，便于快速定位到源文件
 * 4.显示当前是在那个方法体里面
 * 5.最后显示你设置打印出来的信息
 *
 * @Author: GaiQS
 * @E-mail: gaiqs@sina.com
 * @Date: 2017-08-17
 * @Time: 16:45
 * @DateModified: 2017-08-17
 * @Hint:
 * @Param <T>
 * FIXME
 */
public class LoggerHelper {
    private final static boolean isDebug = true;
    public final static String tag = "LinOS";
    private static Hashtable<String, LoggerHelper> sLoggerTable = new Hashtable<>();
    private String mClassName;
    private static final String LOGGER_NAME = "@GaiQS@ ";

    /**
     * 初始化
     *
     * @return LoggerHelper
     */
    public static LoggerHelper instance() {
        return new LoggerHelper(LOGGER_NAME);
    }

    public static LoggerHelper instance(String logger_name) {
        return new LoggerHelper(logger_name);
    }

    private LoggerHelper(String name) {
        mClassName = name;
    }

    /**
     * @param className
     * @return
     */
    @SuppressWarnings("unused")
    private static LoggerHelper getLogger(String className) {
        LoggerHelper classLogger = sLoggerTable.get(className);
        if (classLogger == null) {
            classLogger = new LoggerHelper(className);
            sLoggerTable.put(className, classLogger);
        }
        return classLogger;
    }

    /**
     * Get The Current Function Name
     *
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return mClassName + "[ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() + " ]";
        }
        return null;
    }

    /**
     * The Log Level:i
     *
     * @param str
     */
    public void i(Object str) {
        outLogger(ParamType.INFO, str, null);
    }

    /**
     * The Log Level:d
     *
     * @param str
     */
    public void d(Object str) {
        outLogger(ParamType.DEBUG, str, null);
    }

    /**
     * The Log Level:V
     *
     * @param str
     */
    public void v(Object str) {
        outLogger(ParamType.VERBOSE, str, null);
    }

    /**
     * The Log Level:w
     *
     * @param str
     */
    public void w(Object str) {
        outLogger(ParamType.WARN, str, null);
    }

    /**
     * The Log Level:e
     *
     * @param str
     */
    public void e(Object str) {
        outLogger(ParamType.ERROR, str, null);
    }

    /**
     * The Log Level:Exception
     *
     * @param
     */
    public void e(Exception ex) {
        outLogger(ParamType.EXCEPTION, null, ex);
    }

    /**
     * The Log Level:e
     *
     * @param log
     * @param tr
     */
    public void e(String log, Throwable tr) {
        if (isDebug) {
            String line = getFunctionName();
            Log.e(tag, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + mClassName + line + ":] " + log + "\n", tr);
        }
    }

    private void outLogger(ParamType type, Object str, Exception ex) {
        if (isDebug) {
            String name = getFunctionName();
            switch (type) {
                case INFO:
                    if (name != null) {
                        Log.i(tag, name + " - " + str);
                    } else {
                        Log.i(tag, str.toString());
                    }
                    break;
                case DEBUG:
                    if (name != null) {
                        Log.d(tag, name + " - " + str);
                    } else {
                        Log.d(tag, str.toString());
                    }
                    break;
                case VERBOSE:
                    if (name != null) {
                        Log.v(tag, name + " - " + str);
                    } else {
                        Log.v(tag, str.toString());
                    }
                    break;
                case WARN:
                    if (name != null) {
                        Log.w(tag, name + " - " + str);
                    } else {
                        Log.w(tag, str.toString());
                    }
                    break;
                case ERROR:
                    if (name != null) {
                        Log.e(tag, name + " - " + str);
                    } else {
                        Log.e(tag, str.toString());
                    }
                    break;
                case EXCEPTION:
                    Log.e(tag, "error", ex);
                    break;
            }
        }
    }

    public enum ParamType {
        INFO, DEBUG, VERBOSE, WARN, ERROR, EXCEPTION
    }

    /**
     * 分段打印出较长log文本
     *
     * @param log       原log文本
     * @param showCount 规定每段显示的长度（最好不要超过eclipse限制长度）
     */
    public static void showLogCompletion(String log, int showCount) {
        if (log.length() > showCount) {
            String show = log.substring(0, showCount);
//          System.out.println(show);
            Log.i("TAG", show + "");
            if ((log.length() - showCount) > showCount) {//剩下的文本还是大于规定长度
                String partLog = log.substring(showCount, log.length());
                showLogCompletion(partLog, showCount);
            } else {
                String surplusLog = log.substring(showCount, log.length());
//              System.out.println(surplusLog);
                Log.i("TAG", surplusLog + "");
            }

        } else {
//          System.out.println(log);
            Log.i("TAG", log + "");
        }
    }
}
