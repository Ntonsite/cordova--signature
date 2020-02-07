package org.apache.cordova.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build.VERSION;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Notification extends CordovaPlugin {
    public int confirmResult = -1;
    public ProgressDialog progressDialog = null;
    public ProgressDialog spinnerDialog = null;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (this.cordova.getActivity().isFinishing()) {
            return true;
        }
        if (action.equals("beep")) {
            beep(args.getLong(0));
        } else if (action.equals("alert")) {
            alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
            return true;
        } else if (action.equals("confirm")) {
            confirm(args.getString(0), args.getString(1), args.getJSONArray(2), callbackContext);
            return true;
        } else if (action.equals("prompt")) {
            prompt(args.getString(0), args.getString(1), args.getJSONArray(2), args.getString(3), callbackContext);
            return true;
        } else if (action.equals("activityStart")) {
            activityStart(args.getString(0), args.getString(1));
        } else if (action.equals("activityStop")) {
            activityStop();
        } else if (action.equals("progressStart")) {
            progressStart(args.getString(0), args.getString(1));
        } else if (action.equals("progressValue")) {
            progressValue(args.getInt(0));
        } else if (!action.equals("progressStop")) {
            return false;
        } else {
            progressStop();
        }
        callbackContext.success();
        return true;
    }

    public void beep(final long count) {
        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Ringtone notification = RingtoneManager.getRingtone(Notification.this.cordova.getActivity().getBaseContext(), RingtoneManager.getDefaultUri(2));
                if (notification != null) {
                    for (long i = 0; i < count; i++) {
                        notification.play();
                        long timeout = 5000;
                        while (notification.isPlaying() && timeout > 0) {
                            timeout -= 100;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }
        });
    }

    public synchronized void alert(String message, String title, String buttonLabel, CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;
        final String str = message;
        final String str2 = title;
        final String str3 = buttonLabel;
        final CallbackContext callbackContext2 = callbackContext;
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            /* renamed from: org.apache.cordova.dialogs.Notification$2$1 */
            class C00491 implements OnClickListener {
                C00491() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 0));
                }
            }

            /* renamed from: org.apache.cordova.dialogs.Notification$2$2 */
            class C00502 implements OnCancelListener {
                C00502() {
                }

                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 0));
                }
            }

            public void run() {
                Builder dlg = Notification.this.createDialog(cordova);
                dlg.setMessage(str);
                dlg.setTitle(str2);
                dlg.setCancelable(true);
                dlg.setPositiveButton(str3, new C00491());
                dlg.setOnCancelListener(new C00502());
                Notification.this.changeTextDirection(dlg);
            }
        });
    }

    public synchronized void confirm(String message, String title, JSONArray buttonLabels, CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;
        final String str = message;
        final String str2 = title;
        final JSONArray jSONArray = buttonLabels;
        final CallbackContext callbackContext2 = callbackContext;
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            /* renamed from: org.apache.cordova.dialogs.Notification$3$1 */
            class C00521 implements OnClickListener {
                C00521() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 1));
                }
            }

            /* renamed from: org.apache.cordova.dialogs.Notification$3$2 */
            class C00532 implements OnClickListener {
                C00532() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 2));
                }
            }

            /* renamed from: org.apache.cordova.dialogs.Notification$3$3 */
            class C00543 implements OnClickListener {
                C00543() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 3));
                }
            }

            /* renamed from: org.apache.cordova.dialogs.Notification$3$4 */
            class C00554 implements OnCancelListener {
                C00554() {
                }

                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    callbackContext2.sendPluginResult(new PluginResult(Status.OK, 0));
                }
            }

            public void run() {
                Builder dlg = Notification.this.createDialog(cordova);
                dlg.setMessage(str);
                dlg.setTitle(str2);
                dlg.setCancelable(true);
                if (jSONArray.length() > 0) {
                    try {
                        dlg.setNegativeButton(jSONArray.getString(0), new C00521());
                    } catch (JSONException e) {
                    }
                }
                if (jSONArray.length() > 1) {
                    try {
                        dlg.setNeutralButton(jSONArray.getString(1), new C00532());
                    } catch (JSONException e2) {
                    }
                }
                if (jSONArray.length() > 2) {
                    try {
                        dlg.setPositiveButton(jSONArray.getString(2), new C00543());
                    } catch (JSONException e3) {
                    }
                }
                dlg.setOnCancelListener(new C00554());
                Notification.this.changeTextDirection(dlg);
            }
        });
    }

    public synchronized void prompt(String message, String title, JSONArray buttonLabels, String defaultText, CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;
        final String str = defaultText;
        final String str2 = message;
        final String str3 = title;
        final JSONArray jSONArray = buttonLabels;
        final CallbackContext callbackContext2 = callbackContext;
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final EditText promptInput = new EditText(cordova.getActivity());
                promptInput.setHint(str);
                Builder dlg = Notification.this.createDialog(cordova);
                dlg.setMessage(str2);
                dlg.setTitle(str3);
                dlg.setCancelable(true);
                dlg.setView(promptInput);
                final JSONObject result = new JSONObject();
                if (jSONArray.length() > 0) {
                    try {
                        dlg.setNegativeButton(jSONArray.getString(0), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 1);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? str : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext2.sendPluginResult(new PluginResult(Status.OK, result));
                            }
                        });
                    } catch (JSONException e) {
                    }
                }
                if (jSONArray.length() > 1) {
                    try {
                        dlg.setNeutralButton(jSONArray.getString(1), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 2);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? str : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext2.sendPluginResult(new PluginResult(Status.OK, result));
                            }
                        });
                    } catch (JSONException e2) {
                    }
                }
                if (jSONArray.length() > 2) {
                    try {
                        dlg.setPositiveButton(jSONArray.getString(2), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 3);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? str : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext2.sendPluginResult(new PluginResult(Status.OK, result));
                            }
                        });
                    } catch (JSONException e3) {
                    }
                }
                dlg.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        try {
                            result.put("buttonIndex", 0);
                            result.put("input1", promptInput.getText().toString().trim().length() == 0 ? str : promptInput.getText());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callbackContext2.sendPluginResult(new PluginResult(Status.OK, result));
                    }
                });
                Notification.this.changeTextDirection(dlg);
            }
        });
    }

    public synchronized void activityStart(String title, String message) {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
        final Notification notification = this;
        final CordovaInterface cordova = this.cordova;
        final String str = title;
        final String str2 = message;
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            /* renamed from: org.apache.cordova.dialogs.Notification$5$1 */
            class C00621 implements OnCancelListener {
                C00621() {
                }

                public void onCancel(DialogInterface dialog) {
                    notification.spinnerDialog = null;
                }
            }

            public void run() {
                notification.spinnerDialog = Notification.this.createProgressDialog(cordova);
                notification.spinnerDialog.setTitle(str);
                notification.spinnerDialog.setMessage(str2);
                notification.spinnerDialog.setCancelable(true);
                notification.spinnerDialog.setIndeterminate(true);
                notification.spinnerDialog.setOnCancelListener(new C00621());
                notification.spinnerDialog.show();
            }
        });
    }

    public synchronized void activityStop() {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
    }

    public synchronized void progressStart(String title, String message) {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
        final Notification notification = this;
        final CordovaInterface cordova = this.cordova;
        final String str = title;
        final String str2 = message;
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            /* renamed from: org.apache.cordova.dialogs.Notification$6$1 */
            class C00641 implements OnCancelListener {
                C00641() {
                }

                public void onCancel(DialogInterface dialog) {
                    notification.progressDialog = null;
                }
            }

            public void run() {
                notification.progressDialog = Notification.this.createProgressDialog(cordova);
                notification.progressDialog.setProgressStyle(1);
                notification.progressDialog.setTitle(str);
                notification.progressDialog.setMessage(str2);
                notification.progressDialog.setCancelable(true);
                notification.progressDialog.setMax(100);
                notification.progressDialog.setProgress(0);
                notification.progressDialog.setOnCancelListener(new C00641());
                notification.progressDialog.show();
            }
        });
    }

    public synchronized void progressValue(int value) {
        if (this.progressDialog != null) {
            this.progressDialog.setProgress(value);
        }
    }

    public synchronized void progressStop() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
    }

    @SuppressLint({"NewApi"})
    private Builder createDialog(CordovaInterface cordova) {
        if (VERSION.SDK_INT >= 11) {
            return new Builder(cordova.getActivity(), 5);
        }
        return new Builder(cordova.getActivity());
    }

    @SuppressLint({"InlinedApi"})
    private ProgressDialog createProgressDialog(CordovaInterface cordova) {
        if (VERSION.SDK_INT >= 14) {
            return new ProgressDialog(cordova.getActivity(), 5);
        }
        return new ProgressDialog(cordova.getActivity());
    }

    @SuppressLint({"NewApi"})
    private void changeTextDirection(Builder dlg) {
        int currentapiVersion = VERSION.SDK_INT;
        dlg.create();
        AlertDialog dialog = dlg.show();
        if (currentapiVersion >= 17) {
            ((TextView) dialog.findViewById(16908299)).setTextDirection(5);
        }
    }
}
