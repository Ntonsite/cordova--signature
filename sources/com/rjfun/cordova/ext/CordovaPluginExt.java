package com.rjfun.cordova.ext;

import android.app.Activity;
import android.view.View;
import java.lang.reflect.Method;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

public class CordovaPluginExt extends CordovaPlugin implements PluginAdapterDelegate {
    protected PluginAdapterDelegate adapter = null;

    public void setAdapter(PluginAdapterDelegate theAdapter) {
        this.adapter = theAdapter;
    }

    public PluginAdapterDelegate getAdapter() {
        return this.adapter;
    }

    public View getView() {
        if (this.adapter != null) {
            return this.adapter.getView();
        }
        if (View.class.isAssignableFrom(CordovaWebView.class)) {
            return (View) this.webView;
        }
        try {
            Method getViewMethod = CordovaWebView.class.getMethod("getView", (Class[]) null);
            if (getViewMethod != null) {
                return (View) getViewMethod.invoke(this.webView, new Object[0]);
            }
        } catch (Exception e) {
        }
        return getActivity().getWindow().getDecorView().findViewById(16908290);
    }

    public Activity getActivity() {
        if (this.adapter != null) {
            return this.adapter.getActivity();
        }
        return this.cordova.getActivity();
    }

    public void fireEvent(String obj, String eventName, String jsonData) {
        if (this.adapter != null) {
            this.adapter.fireEvent(obj, eventName, jsonData);
            return;
        }
        String js;
        if ("window".equals(obj)) {
            js = "var evt=document.createEvent('UIEvents');evt.initUIEvent('" + eventName + "',true,false,window,0);window.dispatchEvent(evt);";
        } else {
            js = "javascript:cordova.fireDocumentEvent('" + eventName + "'";
            if (jsonData != null) {
                js = js + "," + jsonData;
            }
            js = js + ");";
        }
        this.webView.loadUrl(js);
    }

    public void sendPluginResult(PluginResult result, CallbackContext context) {
        if (this.adapter != null) {
            this.adapter.sendPluginResult(result, context);
        } else {
            context.sendPluginResult(result);
        }
    }
}
