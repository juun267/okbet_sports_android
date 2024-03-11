package org.cxct.sportlottery.util;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Process;

import androidx.activity.ComponentActivity;

import org.cxct.sportlottery.application.MultiLanguagesApplication;
import org.cxct.sportlottery.service.ServiceBroadcastReceiver;
import org.cxct.sportlottery.view.floatingbtn.RedEnvelopeManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public class AppManager {
    private static int actAccount = 0;
    private static WeakReference<Activity> currentActivity = null;
    private static boolean isInit = false;
    private static Application sApplication;
    private static Stack<Activity> activityStack;
    private static AppManager instance;
    private static AppManager.ActivityLifecycleImpl activityLifecycle = new AppManager.ActivityLifecycleImpl();
    private final List<AppManager.OnAppStatusChangedListener> mStatusListeners = new ArrayList();
    private static boolean isForeground = false;
    //记录app进入后台的时间戳
    private long appBackgroundTime = 0L;

    private AppManager(Application context) {
        if (context == null) {
            throw new UnsupportedOperationException("u can't instantiate me...");
        } else {
            sApplication = context;
            sApplication.registerActivityLifecycleCallbacks(activityLifecycle);
            addOnAppStatusChangedListener(new AppManager.OnAppStatusChangedListener(){
                @Override
                public void onForeground(Activity var1) {
                    long interval = System.currentTimeMillis()-appBackgroundTime;
                    if (appBackgroundTime > 0 && interval > 60*1000){
                        ServiceBroadcastReceiver.INSTANCE.postRefrehInForeground(interval);
                    }
                    appBackgroundTime = 0L;
                }

                @Override
                public void onBackground(Activity var1) {
                    appBackgroundTime = System.currentTimeMillis();
                }
            });
        }
    }

    public static AppManager init(Application context) {
        if (instance == null) {
            instance = new AppManager(context);
        }

        isInit = true;
        return instance;
    }

    public static Application getsApplication() {
        if (sApplication == null) {
            throw new IllegalArgumentException("Application is null");
        } else {
            return sApplication;
        }
    }

    public static AppManager getInstance() {
        if (!isInit) {
            throw new IllegalArgumentException("AppManage class not init");
        } else {
            return instance;
        }
    }

    private static Stack<Activity> getActivityStack() {
        return activityStack;
    }

    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack();
        }

        activityStack.add(activity);
    }

    public static void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }

    }

    public static boolean isActivity() {
        if (activityStack != null) {
            return !activityStack.isEmpty();
        } else {
            return false;
        }
    }

    public static Activity currentActivity() {
        return currentActivity.get();
    }

    public static void finishActivity() {
        Activity activity = (Activity)activityStack.lastElement();
        finishActivity(activity);
    }

    public static void finishActivity(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }

    }

    public static void finishActivity(Class<?> cls) {
        Iterator var1 = activityStack.iterator();

        while(var1.hasNext()) {
            Activity activity = (Activity)var1.next();
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }

    }

    public static void finishAllActivity() {
        int i = 0;

        for(int size = activityStack.size(); i < size; ++i) {
            if (null != activityStack.get(i)) {
                finishActivity((Activity)activityStack.get(i));
            }
        }

        activityStack.clear();
    }

    public static Activity getActivity(Class<?> cls) {
        if (activityStack != null) {
            Iterator var1 = activityStack.iterator();

            while(var1.hasNext()) {
                Activity activity = (Activity)var1.next();
                if (activity.getClass().equals(cls)) {
                    return activity;
                }
            }
        }

        return null;
    }

    public void cleanOther(Class<?> cls) {
        if (activityStack != null) {
            Iterator var2 = activityStack.iterator();

            while(var2.hasNext()) {
                Activity activity = (Activity)var2.next();
                if (!activity.getClass().equals(cls)) {
                    activity.finish();
                }
            }
        }

    }

    public static boolean isAppForeground() {
        return actAccount != 0;
    }

    public static AppManager.ActivityLifecycleImpl getActivityLifecycle() {
        return activityLifecycle;
    }

    public static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity topActivity = currentActivity();
            return (Context)(topActivity == null ? getsApplication() : topActivity);
        } else {
            return getsApplication();
        }
    }

    public void addOnAppStatusChangedListener(AppManager.OnAppStatusChangedListener listener) {
        this.mStatusListeners.add(listener);
    }

    public void removeOnAppStatusChangedListener(AppManager.OnAppStatusChangedListener listener) {
        this.mStatusListeners.remove(listener);
    }

    public static void AppExit() {
        try {
            RedEnvelopeManager.Companion.getInstance().stop();
            finishAllActivity();
            Process.killProcess(Process.myPid());
        } catch (Exception var1) {
            activityStack.clear();
            var1.printStackTrace();
        }

    }

    public static boolean getIsForeground() {
        return isForeground;
    }

    public static boolean isDestroy(Activity activity) {
        return activity == null || activity.isFinishing() || VERSION.SDK_INT >= 17 && activity.isDestroyed();
    }

    public interface OnAppStatusChangedListener {
        void onForeground(Activity var1);

        void onBackground(Activity var1);
    }

    public interface OnActivityOnDestroyListener {
        void onActivityDestroyed(Activity var1);
    }

    public static class ActivityLifecycleImpl implements ActivityLifecycleCallbacks {
        final Map<Activity, Set<AppManager.OnActivityOnDestroyListener>> mDestroyedListenerMap = new HashMap();

        public ActivityLifecycleImpl() {
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            AppManager.addActivity(activity);
            if (activity instanceof ComponentActivity) {
                MultiLanguagesApplication.mInstance.setupSystemStatusChange((ComponentActivity) activity);
            }
        }

        public void onActivityStarted(Activity activity) {
            AppManager.currentActivity = new WeakReference(activity);
            if (AppManager.actAccount == 0) {
                this.postStatus(activity, true);
                AppManager.isForeground = true;
            }

            AppManager.actAccount++;
        }

        public void onActivityResumed(Activity activity) {
            AppManager.currentActivity = new WeakReference(activity);
        }

        public void onActivityPaused(Activity activity) {

        }

        public void onActivityStopped(Activity activity) {
            AppManager.actAccount--;
            if (AppManager.actAccount == 0) {
                this.postStatus(activity, false);
                AppManager.isForeground = false;
            }

        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityDestroyed(Activity activity) {
            this.consumeOnActivityDestroyedListener(activity);
            AppManager.removeActivity(activity);
        }

        public void consumeOnActivityDestroyedListener(Activity activity) {
            Iterator iterator = this.mDestroyedListenerMap.entrySet().iterator();

            while(true) {
                Entry entry;
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    entry = (Entry)iterator.next();
                } while(entry.getKey() != activity);

                Set<AppManager.OnActivityOnDestroyListener> value = (Set)entry.getValue();
                Iterator var5 = value.iterator();

                while(var5.hasNext()) {
                    AppManager.OnActivityOnDestroyListener listener = (AppManager.OnActivityOnDestroyListener)var5.next();
                    listener.onActivityDestroyed(activity);
                }
            }
        }

        private void postStatus(Activity activity, boolean isForeground) {
            if (!AppManager.instance.mStatusListeners.isEmpty()) {
                Iterator var3 = AppManager.instance.mStatusListeners.iterator();

                while(var3.hasNext()) {
                    AppManager.OnAppStatusChangedListener statusListener = (AppManager.OnAppStatusChangedListener)var3.next();
                    if (isForeground) {
                        statusListener.onForeground(activity);
                    } else {
                        statusListener.onBackground(activity);
                    }
                }

            }
        }

        public void addOnActivityDestroyedListener(Activity activity, AppManager.OnActivityOnDestroyListener listener) {
            if (activity != null && listener != null) {
                Object listeners;
                if (!this.mDestroyedListenerMap.containsKey(activity)) {
                    listeners = new HashSet();
                    this.mDestroyedListenerMap.put(activity, (Set<OnActivityOnDestroyListener>) listeners);
                } else {
                    listeners = (Set)this.mDestroyedListenerMap.get(activity);
                    if (((Set)listeners).contains(listener)) {
                        return;
                    }
                }

                ((Set)listeners).add(listener);
            }
        }

        public void removeOnActivityDestroyedListener(Activity activity) {
            if (activity != null) {
                this.mDestroyedListenerMap.remove(activity);
            }
        }
    }
}
