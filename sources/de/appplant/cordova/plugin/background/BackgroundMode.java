package de.appplant.cordova.plugin.background;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import de.appplant.cordova.plugin.background.ForegroundService.ForegroundBinder;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BackgroundMode extends CordovaPlugin {
    private static final String JS_NAMESPACE = "cordova.plugins.backgroundMode";
    private static JSONObject defaultSettings = new JSONObject();
    private final ServiceConnection connection = new C00141();
    private boolean inBackground = false;
    private boolean isBind = false;
    private boolean isDisabled = true;
    private ForegroundService service;

    /* renamed from: de.appplant.cordova.plugin.background.BackgroundMode$1 */
    class C00141 implements ServiceConnection {
        C00141() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundMode.this.service = ((ForegroundBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            BackgroundMode.this.fireEvent(Event.FAILURE, "'service disconnected'");
        }
    }

    private enum Event {
        ACTIVATE,
        DEACTIVATE,
        FAILURE
    }

    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
        if (action.equalsIgnoreCase("configure")) {
            configure(args.getJSONObject(0), args.getBoolean(1));
        } else if (action.equalsIgnoreCase("enable")) {
            enableMode();
        } else if (action.equalsIgnoreCase("disable")) {
            disableMode();
        } else {
            BackgroundExt.execute(action, this.cordova, this.webView);
        }
        callback.success();
        return true;
    }

    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        this.inBackground = true;
        startService();
    }

    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        this.inBackground = false;
        stopService();
    }

    public void onDestroy() {
        super.onDestroy();
        stopService();
    }

    private void enableMode() {
        this.isDisabled = false;
        if (this.inBackground) {
            startService();
        }
    }

    private void disableMode() {
        stopService();
        this.isDisabled = true;
    }

    private void configure(JSONObject settings, boolean update) {
        if (update) {
            updateNotification(settings);
        } else {
            setDefaultSettings(settings);
        }
    }

    private void setDefaultSettings(JSONObject settings) {
        defaultSettings = settings;
    }

    protected static JSONObject getSettings() {
        return defaultSettings;
    }

    private void updateNotification(JSONObject settings) {
        if (this.isBind) {
            this.service.updateNotification(settings);
        }
    }

    private void startService() {
        Activity context = this.cordova.getActivity();
        if (!this.isDisabled && !this.isBind) {
            Intent intent = new Intent(context, ForegroundService.class);
            try {
                context.bindService(intent, this.connection, 1);
                fireEvent(Event.ACTIVATE, null);
                context.startService(intent);
            } catch (Exception e) {
                fireEvent(Event.FAILURE, String.format("'%s'", new Object[]{e.getMessage()}));
            }
            this.isBind = true;
        }
    }

    private void stopService() {
        Activity context = this.cordova.getActivity();
        Intent intent = new Intent(context, ForegroundService.class);
        if (this.isBind) {
            fireEvent(Event.DEACTIVATE, null);
            context.unbindService(this.connection);
            context.stopService(intent);
            this.isBind = false;
        }
    }

    private void fireEvent(Event event, String params) {
        String eventName;
        switch (event) {
            case ACTIVATE:
                eventName = "activate";
                break;
            case DEACTIVATE:
                eventName = "deactivate";
                break;
            default:
                eventName = "failure";
                break;
        }
        String active = event == Event.ACTIVATE ? "true" : "false";
        final String js = String.format("%s._isActive=%s;", new Object[]{JS_NAMESPACE, active}) + String.format("%s.fireEvent('%s',%s);", new Object[]{JS_NAMESPACE, eventName, params}) + String.format("%s.on%s(%s);", new Object[]{JS_NAMESPACE, eventName, params});
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                BackgroundMode.this.webView.loadUrl("javascript:" + js);
            }
        });
    }
}
