package de.appplant.cordova.emailcomposer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class EmailComposerImpl {
    private static final String ATTACHMENT_FOLDER = "/email_composer";
    private static final String MAILTO_SCHEME = "mailto:";

    EmailComposerImpl() {
    }

    void cleanupAttachmentFolder(Context ctx) {
        try {
            File dir = new File(ctx.getExternalCacheDir() + ATTACHMENT_FOLDER);
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            Log.w("EmailComposer", "Missing external cache dir");
        }
    }

    boolean[] canSendMail(String id, Context ctx) {
        boolean withScheme = isAppInstalled(id, ctx);
        return new boolean[]{isEmailAccountConfigured(ctx), withScheme};
    }

    Intent getDraftWithProperties(JSONObject params, Context ctx) throws JSONException {
        Intent mail = getEmailIntent();
        String app = params.optString("app", null);
        if (params.has("subject")) {
            setSubject(params.getString("subject"), mail);
        }
        if (params.has("body")) {
            setBody(params.getString("body"), Boolean.valueOf(params.optBoolean("isHtml")), mail);
        }
        if (params.has("to")) {
            setRecipients(params.getJSONArray("to"), mail);
        }
        if (params.has("cc")) {
            setCcRecipients(params.getJSONArray("cc"), mail);
        }
        if (params.has("bcc")) {
            setBccRecipients(params.getJSONArray("bcc"), mail);
        }
        if (params.has("attachments")) {
            setAttachments(params.getJSONArray("attachments"), mail, ctx);
        }
        if (!app.equals(MAILTO_SCHEME) && isAppInstalled(app, ctx)) {
            mail.setPackage(app);
        }
        return mail;
    }

    private void setSubject(String subject, Intent draft) {
        draft.putExtra("android.intent.extra.SUBJECT", subject);
    }

    private void setBody(String body, Boolean isHTML, Intent draft) {
        CharSequence text;
        if (isHTML.booleanValue()) {
            text = Html.fromHtml(body);
        } else {
            Object text2 = body;
        }
        draft.putExtra("android.intent.extra.TEXT", text);
    }

    private void setRecipients(JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];
        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }
        draft.putExtra("android.intent.extra.EMAIL", receivers);
    }

    private void setCcRecipients(JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];
        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }
        draft.putExtra("android.intent.extra.CC", receivers);
    }

    private void setBccRecipients(JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];
        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }
        draft.putExtra("android.intent.extra.BCC", receivers);
    }

    private void setAttachments(JSONArray attachments, Intent draft, Context ctx) throws JSONException {
        ArrayList<Uri> uris = new ArrayList();
        for (int i = 0; i < attachments.length(); i++) {
            uris.add(getUriForPath(attachments.getString(i), ctx));
        }
        if (!uris.isEmpty()) {
            draft.setAction("android.intent.action.SEND_MULTIPLE").setType("message/rfc822").putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
        }
    }

    private Uri getUriForPath(String path, Context ctx) {
        if (path.startsWith("res:")) {
            return getUriForResourcePath(path, ctx);
        }
        if (path.startsWith("file:///")) {
            return getUriForAbsolutePath(path);
        }
        if (path.startsWith("file://")) {
            return getUriForAssetPath(path, ctx);
        }
        if (path.startsWith("base64:")) {
            return getUriForBase64Content(path, ctx);
        }
        return Uri.parse(path);
    }

    private Uri getUriForAbsolutePath(String path) {
        File file = new File(path.replaceFirst("file://", ""));
        if (!file.exists()) {
            Log.e("EmailComposer", "File not found: " + file.getAbsolutePath());
        }
        return Uri.fromFile(file);
    }

    private Uri getUriForAssetPath(String path, Context ctx) {
        Exception e;
        Throwable th;
        String resPath = path.replaceFirst("file:/", "www");
        String fileName = resPath.substring(resPath.lastIndexOf(47) + 1);
        File dir = ctx.getExternalCacheDir();
        if (dir == null) {
            Log.e("EmailComposer", "Missing external cache dir");
            return Uri.EMPTY;
        }
        String storage = dir.toString() + ATTACHMENT_FOLDER;
        File file = new File(storage, fileName);
        new File(storage).mkdir();
        FileOutputStream outStream = null;
        try {
            AssetManager assets = ctx.getAssets();
            FileOutputStream outStream2 = new FileOutputStream(file);
            try {
                copyFile(assets.open(resPath), outStream2);
                outStream2.flush();
                outStream2.close();
                if (outStream2 != null) {
                    safeClose(outStream2);
                    outStream = outStream2;
                }
            } catch (Exception e2) {
                e = e2;
                outStream = outStream2;
                try {
                    Log.e("EmailComposer", "File not found: assets/" + resPath);
                    e.printStackTrace();
                    if (outStream != null) {
                        safeClose(outStream);
                    }
                    return Uri.fromFile(file);
                } catch (Throwable th2) {
                    th = th2;
                    if (outStream != null) {
                        safeClose(outStream);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                if (outStream != null) {
                    safeClose(outStream);
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e("EmailComposer", "File not found: assets/" + resPath);
            e.printStackTrace();
            if (outStream != null) {
                safeClose(outStream);
            }
            return Uri.fromFile(file);
        }
        return Uri.fromFile(file);
    }

    private Uri getUriForResourcePath(String path, Context ctx) {
        Exception e;
        Throwable th;
        String resPath = path.replaceFirst("res://", "");
        String fileName = resPath.substring(resPath.lastIndexOf(47) + 1);
        String resName = fileName.substring(0, fileName.lastIndexOf(46));
        String extension = resPath.substring(resPath.lastIndexOf(46));
        File dir = ctx.getExternalCacheDir();
        if (dir == null) {
            Log.e("EmailComposer", "Missing external cache dir");
            return Uri.EMPTY;
        }
        String storage = dir.toString() + ATTACHMENT_FOLDER;
        int resId = getResId(resPath, ctx);
        File file = new File(storage, resName + extension);
        if (resId == 0) {
            Log.e("EmailComposer", "File not found: " + resPath);
        }
        new File(storage).mkdir();
        FileOutputStream outStream = null;
        try {
            Resources res = ctx.getResources();
            FileOutputStream outStream2 = new FileOutputStream(file);
            try {
                copyFile(res.openRawResource(resId), outStream2);
                outStream2.flush();
                outStream2.close();
                if (outStream2 != null) {
                    safeClose(outStream2);
                    outStream = outStream2;
                }
            } catch (Exception e2) {
                e = e2;
                outStream = outStream2;
                try {
                    e.printStackTrace();
                    if (outStream != null) {
                        safeClose(outStream);
                    }
                    return Uri.fromFile(file);
                } catch (Throwable th2) {
                    th = th2;
                    if (outStream != null) {
                        safeClose(outStream);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                if (outStream != null) {
                    safeClose(outStream);
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (outStream != null) {
                safeClose(outStream);
            }
            return Uri.fromFile(file);
        }
        return Uri.fromFile(file);
    }

    private Uri getUriForBase64Content(String content, Context ctx) {
        Exception e;
        Throwable th;
        String resName = content.substring(content.indexOf(":") + 1, content.indexOf("//"));
        String resData = content.substring(content.indexOf("//") + 2);
        File dir = ctx.getExternalCacheDir();
        try {
            byte[] bytes = Base64.decode(resData, 0);
            if (dir == null) {
                Log.e("EmailComposer", "Missing external cache dir");
                return Uri.EMPTY;
            }
            String storage = dir.toString() + ATTACHMENT_FOLDER;
            File file = new File(storage, resName);
            new File(storage).mkdir();
            FileOutputStream outStream = null;
            try {
                FileOutputStream outStream2 = new FileOutputStream(file);
                try {
                    outStream2.write(bytes);
                    outStream2.flush();
                    outStream2.close();
                    if (outStream2 != null) {
                        safeClose(outStream2);
                        outStream = outStream2;
                    }
                } catch (Exception e2) {
                    e = e2;
                    outStream = outStream2;
                    try {
                        e.printStackTrace();
                        if (outStream != null) {
                            safeClose(outStream);
                        }
                        return Uri.fromFile(file);
                    } catch (Throwable th2) {
                        th = th2;
                        if (outStream != null) {
                            safeClose(outStream);
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outStream = outStream2;
                    if (outStream != null) {
                        safeClose(outStream);
                    }
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
                if (outStream != null) {
                    safeClose(outStream);
                }
                return Uri.fromFile(file);
            }
            return Uri.fromFile(file);
        } catch (Exception e4) {
            Log.e("EmailComposer", "Invalid Base64 string");
            return Uri.EMPTY;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int read = in.read(buffer);
            if (read != -1) {
                out.write(buffer, 0, read);
            } else {
                return;
            }
        }
    }

    private int getResId(String resPath, Context ctx) {
        Resources res = ctx.getResources();
        String pkgName = ctx.getPackageName();
        String dirName = "drawable";
        String fileName = resPath;
        if (resPath.contains("/")) {
            dirName = resPath.substring(0, resPath.lastIndexOf(47));
            fileName = resPath.substring(resPath.lastIndexOf(47) + 1);
        }
        String resName = fileName.substring(0, fileName.lastIndexOf(46));
        int resId = res.getIdentifier(resName, dirName, pkgName);
        if (resId == 0) {
            resId = res.getIdentifier(resName, "mipmap", pkgName);
        }
        if (resId == 0) {
            return res.getIdentifier(resName, "drawable", pkgName);
        }
        return resId;
    }

    private boolean isEmailAccountConfigured(Context ctx) {
        AccountManager am = AccountManager.get(ctx);
        try {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            for (Account account : am.getAccounts()) {
                if (emailPattern.matcher(account.name).matches()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("EmailComposer", "Missing GET_ACCOUNTS permission.");
            return true;
        }
    }

    private boolean isAppInstalled(String id, Context ctx) {
        if (id.equalsIgnoreCase(MAILTO_SCHEME)) {
            if (ctx.getPackageManager().queryIntentActivities(getEmailIntent(), 0).size() > 0) {
                return true;
            }
            return false;
        }
        try {
            ctx.getPackageManager().getPackageInfo(id, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static Intent getEmailIntent() {
        Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse(MAILTO_SCHEME));
        intent.addFlags(268435456);
        return intent;
    }

    private static boolean safeClose(FileOutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
                return true;
            } catch (IOException e) {
                Log.e("EmailComposer", "Error attempting to safely close resource: " + e.getMessage());
            }
        }
        return false;
    }
}
