package de.appplant.cordova.plugin.background;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import org.json.JSONObject;

public class ForegroundService extends Service {
    private static final String NOTIFICATION_ICON = "icon";
    public static final int NOTIFICATION_ID = -574543954;
    private static final String NOTIFICATION_TEXT = "Doing heavy tasks.";
    private static final String NOTIFICATION_TITLE = "App is running in background";
    private final IBinder mBinder = new ForegroundBinder();
    private WakeLock wakeLock;

    public class ForegroundBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate() {
        super.onCreate();
        keepAwake();
    }

    public void onDestroy() {
        super.onDestroy();
        sleepWell();
    }

    private void keepAwake() {
        if (!BackgroundMode.getSettings().optBoolean("silent", false)) {
            startForeground(NOTIFICATION_ID, makeNotification());
        }
        this.wakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "BackgroundMode");
        this.wakeLock.acquire();
    }

    private void sleepWell() {
        stopForeground(true);
        getNotificationManager().cancel(NOTIFICATION_ID);
        if (this.wakeLock != null) {
            this.wakeLock.release();
            this.wakeLock = null;
        }
    }

    private Notification makeNotification() {
        return makeNotification(BackgroundMode.getSettings());
    }

    private Notification makeNotification(JSONObject settings) {
        String title = settings.optString("title", NOTIFICATION_TITLE);
        String text = settings.optString("text", NOTIFICATION_TEXT);
        boolean bigText = settings.optBoolean("bigText", false);
        Context context = getApplicationContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        Builder notification = new Builder(context).setContentTitle(title).setContentText(text).setOngoing(true).setSmallIcon(getIconResId(settings));
        if (settings.optBoolean("hidden", true)) {
            notification.setPriority(-2);
        }
        if (bigText || text.contains("\n")) {
            notification.setStyle(new BigTextStyle().bigText(text));
        }
        setColor(notification, settings);
        if (intent != null && settings.optBoolean("resume")) {
            notification.setContentIntent(PendingIntent.getActivity(context, NOTIFICATION_ID, intent, 134217728));
        }
        return notification.build();
    }

    protected void updateNotification(JSONObject settings) {
        if (settings.optBoolean("silent", false)) {
            stopForeground(true);
            return;
        }
        getNotificationManager().notify(NOTIFICATION_ID, makeNotification(settings));
    }

    private int getIconResId(JSONObject settings) {
        Context context = getApplicationContext();
        Resources res = context.getResources();
        String pkgName = context.getPackageName();
        String icon = settings.optString(NOTIFICATION_ICON, NOTIFICATION_ICON);
        int resId = getIconResId(res, icon, "mipmap", pkgName);
        if (resId == 0) {
            return getIconResId(res, icon, "drawable", pkgName);
        }
        return resId;
    }

    private int getIconResId(Resources res, String icon, String type, String pkgName) {
        int resId = res.getIdentifier(icon, type, pkgName);
        if (resId == 0) {
            return res.getIdentifier(NOTIFICATION_ICON, type, pkgName);
        }
        return resId;
    }

    private void setColor(Builder notification, JSONObject settings) {
        String hex = settings.optString("color", null);
        if (VERSION.SDK_INT >= 21 && hex != null) {
            try {
                int aRGB = Integer.parseInt(hex, 16) - 16777216;
                notification.getClass().getMethod("setColor", new Class[]{Integer.TYPE}).invoke(notification, new Object[]{Integer.valueOf(aRGB)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService("notification");
    }
}
