package org.apache.cordova.splashscreen;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class SplashScreen extends CordovaPlugin {
    private static final boolean HAS_BUILT_IN_SPLASH_SCREEN;
    private static final String LOG_TAG = "SplashScreen";
    private static boolean firstShow = true;
    private static ProgressDialog spinnerDialog;
    private static Dialog splashDialog;
    private int orientation;
    private ImageView splashImageView;

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen$1 */
    class C00791 implements Runnable {
        C00791() {
        }

        public void run() {
            SplashScreen.this.webView.postMessage("splashscreen", "hide");
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen$2 */
    class C00802 implements Runnable {
        C00802() {
        }

        public void run() {
            SplashScreen.this.webView.postMessage("splashscreen", "show");
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen$4 */
    class C00824 implements Runnable {
        C00824() {
        }

        public void run() {
            if (SplashScreen.splashDialog != null && SplashScreen.splashDialog.isShowing()) {
                SplashScreen.splashDialog.dismiss();
                SplashScreen.splashDialog = null;
                SplashScreen.this.splashImageView = null;
            }
        }
    }

    /* renamed from: org.apache.cordova.splashscreen.SplashScreen$7 */
    class C00877 implements Runnable {
        C00877() {
        }

        public void run() {
            if (SplashScreen.spinnerDialog != null && SplashScreen.spinnerDialog.isShowing()) {
                SplashScreen.spinnerDialog.dismiss();
                SplashScreen.spinnerDialog = null;
            }
        }
    }

    static {
        boolean z = false;
        if (Integer.valueOf(CordovaWebView.CORDOVA_VERSION.split("\\.")[0]).intValue() < 4) {
            z = true;
        }
        HAS_BUILT_IN_SPLASH_SCREEN = z;
    }

    private View getView() {
        try {
            return (View) this.webView.getClass().getMethod("getView", new Class[0]).invoke(this.webView, new Object[0]);
        } catch (Exception e) {
            return (View) this.webView;
        }
    }

    protected void pluginInitialize() {
        if (!HAS_BUILT_IN_SPLASH_SCREEN && firstShow) {
            getView().setVisibility(4);
            if (this.preferences.getInteger("SplashDrawableId", 0) == 0) {
                String splashResource = this.preferences.getString(LOG_TAG, "screen");
                if (splashResource != null) {
                    int drawableId = this.cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", this.cordova.getActivity().getClass().getPackage().getName());
                    if (drawableId == 0) {
                        drawableId = this.cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", this.cordova.getActivity().getPackageName());
                    }
                    this.preferences.set("SplashDrawableId", drawableId);
                }
            }
            this.orientation = this.cordova.getActivity().getResources().getConfiguration().orientation;
            firstShow = false;
            loadSpinner();
            showSplashScreen(true);
        }
    }

    private boolean isMaintainAspectRatio() {
        return this.preferences.getBoolean("SplashMaintainAspectRatio", false);
    }

    public void onPause(boolean multitasking) {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            removeSplashScreen();
        }
    }

    public void onDestroy() {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            removeSplashScreen();
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("hide")) {
            this.cordova.getActivity().runOnUiThread(new C00791());
        } else if (action.equals("show")) {
            this.cordova.getActivity().runOnUiThread(new C00802());
        } else if (!action.equals("spinnerStart")) {
            return false;
        } else {
            if (!HAS_BUILT_IN_SPLASH_SCREEN) {
                final String title = args.getString(0);
                final String message = args.getString(1);
                this.cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        SplashScreen.this.spinnerStart(title, message);
                    }
                });
            }
        }
        callbackContext.success();
        return true;
    }

    public Object onMessage(String id, Object data) {
        if (!HAS_BUILT_IN_SPLASH_SCREEN) {
            if ("splashscreen".equals(id)) {
                if ("hide".equals(data.toString())) {
                    removeSplashScreen();
                } else {
                    showSplashScreen(false);
                }
            } else if ("spinner".equals(id)) {
                if ("stop".equals(data.toString())) {
                    spinnerStop();
                    getView().setVisibility(0);
                }
            } else if ("onReceivedError".equals(id)) {
                spinnerStop();
            }
        }
        return null;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation != this.orientation) {
            this.orientation = newConfig.orientation;
            if (this.splashImageView != null) {
                int drawableId = this.preferences.getInteger("SplashDrawableId", 0);
                if (drawableId != 0) {
                    this.splashImageView.setImageDrawable(this.cordova.getActivity().getResources().getDrawable(drawableId));
                }
            }
        }
    }

    private void removeSplashScreen() {
        this.cordova.getActivity().runOnUiThread(new C00824());
    }

    private void showSplashScreen(final boolean hideAfterDelay) {
        final int splashscreenTime = this.preferences.getInteger("SplashScreenDelay", 3000);
        final int drawableId = this.preferences.getInteger("SplashDrawableId", 0);
        if ((splashDialog != null && splashDialog.isShowing()) || drawableId == 0) {
            return;
        }
        if (splashscreenTime > 0 || !hideAfterDelay) {
            this.cordova.getActivity().runOnUiThread(new Runnable() {

                /* renamed from: org.apache.cordova.splashscreen.SplashScreen$5$1 */
                class C00831 implements Runnable {
                    C00831() {
                    }

                    public void run() {
                        SplashScreen.this.removeSplashScreen();
                    }
                }

                public void run() {
                    Display display = SplashScreen.this.cordova.getActivity().getWindowManager().getDefaultDisplay();
                    Context context = SplashScreen.this.webView.getContext();
                    SplashScreen.this.splashImageView = new ImageView(context);
                    SplashScreen.this.splashImageView.setImageResource(drawableId);
                    SplashScreen.this.splashImageView.setLayoutParams(new LayoutParams(-1, -1));
                    SplashScreen.this.splashImageView.setMinimumHeight(display.getHeight());
                    SplashScreen.this.splashImageView.setMinimumWidth(display.getWidth());
                    SplashScreen.this.splashImageView.setBackgroundColor(SplashScreen.this.preferences.getInteger("backgroundColor", -16777216));
                    if (SplashScreen.this.isMaintainAspectRatio()) {
                        SplashScreen.this.splashImageView.setScaleType(ScaleType.CENTER_CROP);
                    } else {
                        SplashScreen.this.splashImageView.setScaleType(ScaleType.FIT_XY);
                    }
                    SplashScreen.splashDialog = new Dialog(context, 16973840);
                    if ((SplashScreen.this.cordova.getActivity().getWindow().getAttributes().flags & 1024) == 1024) {
                        SplashScreen.splashDialog.getWindow().setFlags(1024, 1024);
                    }
                    SplashScreen.splashDialog.setContentView(SplashScreen.this.splashImageView);
                    SplashScreen.splashDialog.setCancelable(false);
                    SplashScreen.splashDialog.show();
                    if (hideAfterDelay) {
                        new Handler().postDelayed(new C00831(), (long) splashscreenTime);
                    }
                }
            });
        }
    }

    private void loadSpinner() {
        String loading;
        if (this.webView.canGoBack()) {
            loading = this.preferences.getString("LoadingDialog", null);
        } else {
            loading = this.preferences.getString("LoadingPageDialog", null);
        }
        if (loading != null) {
            String title = "";
            String message = "Loading Application...";
            if (loading.length() > 0) {
                int comma = loading.indexOf(44);
                if (comma > 0) {
                    title = loading.substring(0, comma);
                    message = loading.substring(comma + 1);
                } else {
                    title = "";
                    message = loading;
                }
            }
            spinnerStart(title, message);
        }
    }

    private void spinnerStart(final String title, final String message) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            /* renamed from: org.apache.cordova.splashscreen.SplashScreen$6$1 */
            class C00851 implements OnCancelListener {
                C00851() {
                }

                public void onCancel(DialogInterface dialog) {
                    SplashScreen.spinnerDialog = null;
                }
            }

            public void run() {
                SplashScreen.this.spinnerStop();
                SplashScreen.spinnerDialog = ProgressDialog.show(SplashScreen.this.webView.getContext(), title, message, true, true, new C00851());
            }
        });
    }

    private void spinnerStop() {
        this.cordova.getActivity().runOnUiThread(new C00877());
    }
}
