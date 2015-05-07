package com.eloser.eloserclient.util;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class Log {
    private static int[] mInfoViewIds = null;
    private static Activity mActivity = null;


    public enum LogLevel {
        None(0), Error(1), Warn(2), Info(3), Debug(4), Verbose(5);

        LogLevel(int level) {
            this.level = level;
        }

        protected int getInt() {
            return this.level;
        }

        private int level;
    }

    private static LogLevel logLevel = LogLevel.None;

    public static void initLog(Activity activity, int[] infoViewIds, LogLevel level) {
        synchronized (Log.class) {
            mActivity = activity;
            mInfoViewIds = infoViewIds;
            logLevel = level;

            if (mActivity != null) {
                for (int i = LogLevel.Error.getInt(); i < LogLevel.Verbose.getInt(); ++i) {
                    final int ind = i;
                    final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[i]);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            info.setText("Init log level: " + ind);
                        }
                    });
                }
            }
        }
    }

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static void e(final String tag, final String message) {
        if (logLevel.getInt() < LogLevel.Error.getInt()) {
            return;
        }

        if (mActivity != null) {
            final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[LogLevel.Error.getInt()]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    info.setText(message);
                }
            });
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.e(tagInfo.toString(), message);
    }


    public static void e(final String tag, final String message, final Exception exception) {
        if (logLevel.getInt() < LogLevel.Error.getInt()) {
            return;
        }

        if (mActivity != null) {
            final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[LogLevel.Error.getInt()]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    info.setText(message);
                }
            });
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.e(tagInfo.toString(), message, exception);
        exception.printStackTrace();
    }


    public static void w(final String tag, final String message) {
        if (logLevel.getInt() < LogLevel.Warn.getInt()) {
            return;
        }

        if (mActivity != null) {
            final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[LogLevel.Warn.getInt()]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    info.setText(message);
                }
            });
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.w(tagInfo.toString(), message);
    }


    public static void i(final String tag, final String message) {
        if (logLevel.getInt() < LogLevel.Info.getInt()) {
            return;
        }

        if (mActivity != null) {
            final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[LogLevel.Info.getInt()]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    info.setText(message);
                }
            });
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.i(tagInfo.toString(), message);
    }


    public static void d(final String tag, final String message) {
        if (logLevel.getInt() < LogLevel.Debug.getInt()) {
            return;
        }

        if (mActivity != null) {
            final TextView info = (TextView) mActivity.findViewById(mInfoViewIds[LogLevel.Debug.getInt()]);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    info.setText(message);
                }
            });
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.d(tagInfo.toString(), message);
    }


    public static void v(final String tag, final String message) {
        if (logLevel.getInt() < LogLevel.Verbose.getInt()) {
            return;
        }

        StringBuilder tagInfo = new StringBuilder();
        tagInfo
                .append(" (").append(tag).append(") ")
                .append(StackTraceInfo.getInvokingFileName())
                .append(" [").append(StackTraceInfo.getInvokingClassName())
                .append("/").append(StackTraceInfo.getInvokingMethodName())
                .append("()] ");

        android.util.Log.v(tagInfo.toString(), message);
    }

} // class Log
