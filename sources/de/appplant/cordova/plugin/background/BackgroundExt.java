package de.appplant.cordova.plugin.background;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.content.Intent;
import android.os.Build.VERSION;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.List;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

class BackgroundExt {
    private final WeakReference<CordovaInterface> cordova;
    private final WeakReference<CordovaWebView> webView;

    /* renamed from: de.appplant.cordova.plugin.background.BackgroundExt$1 */
    class C00131 extends Thread {

        /* renamed from: de.appplant.cordova.plugin.background.BackgroundExt$1$1 */
        class C00121 implements Runnable {
            C00121() {
            }

            public void run() {
                View view = ((CordovaWebView) BackgroundExt.this.webView.get()).getEngine().getView();
                try {
                    Class.forName("org.crosswalk.engine.XWalkCordovaView").getMethod("onShow", new Class[0]).invoke(view, new Object[0]);
                } catch (Exception e) {
                    view.dispatchWindowVisibilityChanged(0);
                }
            }
        }

        C00131() {
        }

        public void run() {
            try {
                Thread.sleep(1000);
                BackgroundExt.this.getActivity().runOnUiThread(new C00121());
            } catch (InterruptedException e) {
            }
        }
    }

    private BackgroundExt(CordovaInterface cordova, CordovaWebView webView) {
        this.cordova = new WeakReference(cordova);
        this.webView = new WeakReference(webView);
    }

    static void execute(String action, CordovaInterface cordova, CordovaWebView webView) {
        BackgroundExt ext = new BackgroundExt(cordova, webView);
        if (action.equalsIgnoreCase("optimizations")) {
            ext.disableWebViewOptimizations();
        }
        if (action.equalsIgnoreCase("background")) {
            ext.moveToBackground();
        }
        if (action.equalsIgnoreCase("foreground")) {
            ext.moveToForeground();
        }
        if (action.equalsIgnoreCase("tasklist")) {
            ext.excludeFromTaskList();
        }
    }

    private void moveToBackground() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        getActivity().startActivity(intent);
    }

    private void moveToForeground() {
        Activity app = getActivity();
        Intent intent = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        intent.addFlags(537001984);
        app.startActivity(intent);
    }

    private void disableWebViewOptimizations() {
        new C00131().start();
    }

    @TargetApi(21)
    private void excludeFromTaskList() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService("activity");
        if (am != null && VERSION.SDK_INT >= 21) {
            List<AppTask> tasks = am.getAppTasks();
            if (tasks != null && !tasks.isEmpty()) {
                ((AppTask) tasks.get(0)).setExcludeFromRecents(true);
            }
        }
    }

    Activity getActivity() {
        return ((CordovaInterface) this.cordova.get()).getActivity();
    }
}
