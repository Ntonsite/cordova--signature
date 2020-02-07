package com.rjfun.cordova.ext;

import android.app.Activity;
import android.view.View;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public interface PluginAdapterDelegate {
    void fireEvent(String str, String str2, String str3);

    Activity getActivity();

    View getView();

    void sendPluginResult(PluginResult pluginResult, CallbackContext callbackContext);
}
