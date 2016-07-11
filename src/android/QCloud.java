package org.bike.giantss;

import android.util.Log;

import com.tencent.tauth.Tencent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QCloud extends CordovaPlugin {

   private static Tencent mTencent;
    private CallbackContext currentCallbackContext;
    private static final String TAG = "QCloud";
    private String APP_ID;
    private static final String QCLOUD_APP_ID = "qcloud_app_id";


    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        APP_ID = webView.getPreferences().getString(QCLOUD_APP_ID, "");
//        mTencent = Tencent.createInstance(APP_ID, this.cordova.getActivity()
//                .getApplicationContext());
    }

    @Override
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        if (action.equals("ssoLogin")) {
            return ssoLogin(callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

    /**
     * QQ 单点登录
     *
     * @param callbackContext
     * @return
     */
    private boolean ssoLogin(CallbackContext callbackContext) {
        Log.d(TAG,APP_ID);
        currentCallbackContext = callbackContext;
        if (mTencent.isSessionValid()) {
            JSONObject jo = makeJson(mTencent.getAccessToken(),
                    mTencent.getOpenId(),mTencent.getExpiresIn());
            this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.OK, jo), callbackContext.getCallbackId());
            return true;
        } else {

            this.cordova.setActivityResultCallback(this);
            return true;
        }

    }

    /**
     * 组装JSON
     *
     * @param access_token
     * @param userid
     * @param expires_time
     * @return
     */
    private JSONObject makeJson(String access_token, String userid, long expires_time) {
        String json = "{\"access_token\": \"" + access_token + "\", " +
                " \"userid\": \"" + userid + "\", " +
                " \"expires_time\": \"" + String.valueOf(expires_time) + "\"" +
                "}";
        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }
}
