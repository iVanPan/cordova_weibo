package org.zy.yuancheng.weibo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class YCWeibo extends CordovaPlugin {

    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    private static final String WEBIO_APP_ID = "weibo_app_id";
    private static final String WEBIO_REDIRECT_URL = "redirecturi";
    private static final String DEFAULT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String CANCEL_BY_USER = "cancel by user";
    private static final String UNKONW_ERROR = "unkonw error";
    private static final String WEIBO_EXCEPTION = "weibo exception";
    private static final String ONLY_GET_CODE = "only get code";
    private static final String ERROR_IMAGE_URL = "share image url is incorrect";
    private static final String WEIBO_CLIENT_NOT_INSTALLED = "weibo client is not installed";
    private static final String DEFAULT_WEBPAGE_ICON = "http://www.sinaimg.cn/blog/developer/wiki/LOGO_64x64.png";
    public static CallbackContext currentCallbackContext;
    public static String APP_KEY;
    public static IWeiboShareAPI mWeiboShareAPI = null;
    private Oauth2AccessToken mAccessToken;
    private String REDIRECT_URL;
    private SsoHandler mSsoHandler;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        APP_KEY = webView.getPreferences().getString(WEBIO_APP_ID, "");
        REDIRECT_URL = webView.getPreferences().getString(WEBIO_REDIRECT_URL, DEFAULT_URL);
    }

    @Override
    public boolean execute(String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        if (action.equalsIgnoreCase("ssoLogin")) {
            return ssoLogin(callbackContext);
        } else if (action.equalsIgnoreCase("logout")) {
            return logout(callbackContext);
        } else if (action.equalsIgnoreCase("shareToWeibo")) {
            return shareToWeibo(callbackContext, args);
        } else if (action.equalsIgnoreCase("checkClientInstalled")) {
            return checkClientInstalled(callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

    /**
     * 组装JSON
     *
     * @param access_token
     * @param userid
     * @return
     */
    private JSONObject makeJson(String access_token, String userid) {
        String json = "{\"access_token\": \"" + access_token
                + "\",  \"userid\": \"" + userid + "\"}";
        JSONObject jo = null;
        try {
            jo = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    /**
     * weibo sso 登录
     *
     * @param callbackContext
     * @return
     */
    private boolean ssoLogin(CallbackContext callbackContext) {
        currentCallbackContext = callbackContext;
        AuthInfo mAuthInfo = new AuthInfo(YCWeibo.this.cordova.getActivity(),
                APP_KEY, REDIRECT_URL, SCOPE);
        mSsoHandler = new SsoHandler(YCWeibo.this.cordova.getActivity(),
                mAuthInfo);
        mAccessToken = AccessTokenKeeper.readAccessToken(YCWeibo.this.cordova
                .getActivity());
        if (mAccessToken.isSessionValid()) {
            JSONObject jo = makeJson(mAccessToken.getToken(),
                    mAccessToken.getUid());
            this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.OK, jo), callbackContext
                    .getCallbackId());
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (mSsoHandler != null) {
                        mSsoHandler.authorize(AuthListener);
                    }
                }

                ;
            };
            this.cordova.setActivityResultCallback(this);
            this.cordova.getActivity().runOnUiThread(runnable);
        }
        return true;
    }

    /**
     * 检查微博客户端是否安装
     *
     * @param callbackContext
     * @return
     */
    private boolean checkClientInstalled(CallbackContext callbackContext) {
        AuthInfo mAuthInfo = new AuthInfo(YCWeibo.this.cordova.getActivity(),
                APP_KEY, REDIRECT_URL, SCOPE);
        if (mSsoHandler == null) {
            mSsoHandler = new SsoHandler(YCWeibo.this.cordova.getActivity(),
                    mAuthInfo);
        }
        Boolean installed = mSsoHandler.isWeiboAppInstalled();
        if (installed) {
            callbackContext.success();
        } else {
            callbackContext.error(WEIBO_CLIENT_NOT_INSTALLED);
        }
        return true;
    }

    /**
     * 微博登出
     *
     * @param callbackContext
     * @return
     */
    private boolean logout(CallbackContext callbackContext) {
        AccessTokenKeeper.clear(this.cordova.getActivity());
        callbackContext.success();
        return true;
    }

    /**
     * 微博分享
     *
     * @param callbackContext
     * @param args
     * @return
     */
    private boolean shareToWeibo(CallbackContext callbackContext,
                                 final JSONArray args) {
        currentCallbackContext = callbackContext;
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(
                this.cordova.getActivity(), APP_KEY);
        mWeiboShareAPI.registerApp();
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    sendSingleMessage(args.getJSONObject(0));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, intent);
        }

    }

    /**
     * 生成微博要分享出去的网页对象
     *
     * @param params
     * @return
     * @throws JSONException
     */
    private WebpageObject getWebpageObj(JSONObject params) throws JSONException {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = params.getString("title");
        mediaObject.description = params.getString("description");
        if (params.getString("imageUrl") != null
                && !params.getString("imageUrl").equalsIgnoreCase("")) {
            try {
                if (params.getString("imageUrl").startsWith("http://")
                        || params.getString("imageUrl").startsWith("https://")) {
                    Bitmap thumb = BitmapFactory.decodeStream(new URL(params
                            .getString("imageUrl")).openConnection()
                            .getInputStream());
                    mediaObject.setThumbImage(thumb);
                } else {
                    currentCallbackContext.error(ERROR_IMAGE_URL);
                }

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                Bitmap thumb = BitmapFactory
                        .decodeStream(new URL(DEFAULT_WEBPAGE_ICON)
                                .openConnection().getInputStream());
                mediaObject.setThumbImage(thumb);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mediaObject.actionUrl = params.getString("url");
        mediaObject.defaultText = params.getString("defaultText");
        return mediaObject;
    }

    /**
     * 发送微博单条消息请求
     *
     * @param params
     */
    private void sendSingleMessage(JSONObject params) {
        WeiboMessage weiboMessage = new WeiboMessage();
        try {
            weiboMessage.mediaObject = getWebpageObj(params);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;
        if (mWeiboShareAPI.isWeiboAppInstalled()) {
            if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
                mWeiboShareAPI.sendRequest(this.cordova.getActivity(), request);
            } else {
                currentCallbackContext.error(UNKONW_ERROR);
            }
        } else {
            sendSingleMsgWithOutClient(request);
        }
    }

    /**
     * 在没有客户端的情况下分享消息到微博
     *
     * @param request
     */
    private void sendSingleMsgWithOutClient(SendMessageToWeiboRequest request) {
        AuthInfo mAuthInfo = new AuthInfo(YCWeibo.this.cordova.getActivity(),
                APP_KEY, REDIRECT_URL, SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper
                .readAccessToken(this.cordova.getActivity()
                        .getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(this.cordova.getActivity(), request,
                mAuthInfo, token, new WeiboAuthListener() {

                    @Override
                    public void onWeiboException(WeiboException arg0) {
                        currentCallbackContext.error(WEIBO_EXCEPTION);
                    }

                    @Override
                    public void onComplete(Bundle bundle) {
                        Oauth2AccessToken newToken = Oauth2AccessToken
                                .parseAccessToken(bundle);
                        AccessTokenKeeper.writeAccessToken(YCWeibo.this.cordova
                                        .getActivity().getApplicationContext(),
                                newToken);
                    }

                    @Override
                    public void onCancel() {
                        currentCallbackContext.error(CANCEL_BY_USER);
                    }
                });
    }

    /**
     * 微博auth监听
     */
    WeiboAuthListener AuthListener = new WeiboAuthListener() {

        @Override
        public void onComplete(Bundle values) {
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(
                        YCWeibo.this.cordova.getActivity(), mAccessToken);
                JSONObject jo = makeJson(mAccessToken.getToken(),
                        mAccessToken.getUid());
                YCWeibo.this.webView.sendPluginResult(new PluginResult(
                        PluginResult.Status.OK, jo), currentCallbackContext.getCallbackId());
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                // String code = values.getString("code");
                YCWeibo.this.webView.sendPluginResult(new PluginResult(
                                PluginResult.Status.ERROR, ONLY_GET_CODE),
                        currentCallbackContext.getCallbackId());

            }
        }

        @Override
        public void onCancel() {
            YCWeibo.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, CANCEL_BY_USER),
                    currentCallbackContext.getCallbackId());
        }

        @Override
        public void onWeiboException(WeiboException e) {
            YCWeibo.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, WEIBO_EXCEPTION),
                    currentCallbackContext.getCallbackId());
        }
    };
}