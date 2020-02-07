package de.appplant.cordova.emailcomposer;

import android.content.Context;
import android.content.Intent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmailComposer extends CordovaPlugin {
    static final String LOG_TAG = "EmailComposer";
    private CallbackContext command;
    private final EmailComposerImpl impl = new EmailComposerImpl();

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.impl.cleanupAttachmentFolder(getContext());
    }

    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
        this.command = callback;
        if ("open".equalsIgnoreCase(action)) {
            open(args);
            return true;
        } else if (!"isAvailable".equalsIgnoreCase(action)) {
            return false;
        } else {
            isAvailable(args.getString(0));
            return true;
        }
    }

    private Context getContext() {
        return this.cordova.getActivity();
    }

    private void isAvailable(final String id) {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                boolean[] available = EmailComposer.this.impl.canSendMail(id, EmailComposer.this.getContext());
                List messages = new ArrayList();
                messages.add(new PluginResult(Status.OK, available[0]));
                messages.add(new PluginResult(Status.OK, available[1]));
                PluginResult result = new PluginResult(Status.OK, messages);
                try {
                    Field field = result.getClass().getDeclaredField("encodedMessage");
                    field.setAccessible(true);
                    field.set(result, String.format("%b,%b", new Object[]{Boolean.valueOf(available[0]), Boolean.valueOf(available[1])}));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EmailComposer.this.command.sendPluginResult(result);
            }
        });
    }

    private void open(JSONArray args) throws JSONException {
        JSONObject props = args.getJSONObject(0);
        if (this.impl.canSendMail(props.getString("app"), getContext())[0]) {
            final Intent chooser = Intent.createChooser(this.impl.getDraftWithProperties(props, getContext()), props.optString("chooserHeader", "Open with"));
            final EmailComposer plugin = this;
            this.cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    EmailComposer.this.cordova.startActivityForResult(plugin, chooser, 0);
                }
            });
            return;
        }
        LOG.m6i(LOG_TAG, "No client or account found for.");
    }

    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        if (this.command != null) {
            this.command.success();
        }
    }
}
