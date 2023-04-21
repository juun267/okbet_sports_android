package org.cxct.sportlottery.util;

import com.google.gson.Gson;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.cxct.sportlottery.BuildConfig;


public class LogUtil {
    static {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("oklog")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }


    public static void d(String msg) {
        Logger.d(msg);
    }

    public static void d(String msg, Throwable t) {
        Logger.d(msg, t);
    }

    public static void e(String msg) {
        Logger.e(msg);
    }

    public static void e(String msg, Throwable t) {
        Logger.e(msg, t);
    }

    public static void i(String msg) {
        Logger.i(msg);
    }

    public static void i(String msg, Throwable t) {
        Logger.i(msg, t);
    }

    public static void v(String msg) {
        Logger.v(msg);
    }

    public static void v(String msg, Throwable t) {
        Logger.v(msg, t);
    }

    public static void w(String msg) {
        Logger.w(msg);
    }

    public static void w(String msg, Throwable t) {
        Logger.w(msg, t);
    }

    public static void json(String json) {
        Logger.json(json);
    }

    public static void xml(String xml) {
        Logger.xml(xml);
    }

    public static void toJson(Object object) {
        Logger.json(new Gson().toJson(object));
    }
}
