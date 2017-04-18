package me.vanpan.weibosdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.URLUtil;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class WeiboSDKPlugin extends CordovaPlugin {

    private static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    private static final String WEBIO_APP_ID = "weibo_app_id";
    private static final String WEBIO_REDIRECT_URL = "redirecturi";
    private static final String DEFAULT_URL = "https://api.weibo.com/oauth2/default.html";
    private static final String CANCEL_BY_USER = "cancel by user";
    private static final String WEIBO_EXCEPTION = "weibo exception";
    private static final String PARAM_ERROR = "param error";
    private static final String ONLY_GET_CODE = "only get code";
    private static final String WEIBO_CLIENT_NOT_INSTALLED = "weibo client is not installed";
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
    public boolean execute(String action, final CordovaArgs args,
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
     * weibo sso 登录
     *
     * @param callbackContext
     * @return
     */
    private boolean ssoLogin(CallbackContext callbackContext) {
        currentCallbackContext = callbackContext;
        AuthInfo mAuthInfo = new AuthInfo(WeiboSDKPlugin.this.cordova.getActivity(),
                APP_KEY, REDIRECT_URL, SCOPE);
        mSsoHandler = new SsoHandler(WeiboSDKPlugin.this.cordova.getActivity(),
                mAuthInfo);
        Runnable runnable = new Runnable() {
            public void run() {
                if (mSsoHandler != null) {
                    mSsoHandler.authorize(AuthListener);
                }
            }
        };
        this.cordova.setActivityResultCallback(this);
        this.cordova.getActivity().runOnUiThread(runnable);
        return true;
    }

    /**
     * 检查微博客户端是否安装
     *
     * @param callbackContext
     * @return
     */
    private boolean checkClientInstalled(CallbackContext callbackContext) {
        AuthInfo mAuthInfo = new AuthInfo(WeiboSDKPlugin.this.cordova.getActivity(),
                APP_KEY, REDIRECT_URL, SCOPE);
        if (mSsoHandler == null) {
            mSsoHandler = new SsoHandler(WeiboSDKPlugin.this.cordova.getActivity(),
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
    private boolean shareToWeibo(final CallbackContext callbackContext,
                                 final CordovaArgs args) {
        currentCallbackContext = callbackContext;
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(
                this.cordova.getActivity(), APP_KEY);
        mWeiboShareAPI.registerApp();
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                sendMultiMessage(callbackContext,args);
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
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sendMultiMessage(final CallbackContext callbackContext, CordovaArgs args) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        final JSONObject data;
        try {
            data = args.getJSONObject(0);
            String text = data.has("title")?  data.getString("title"): "";
            TextObject textObject = new TextObject();
            textObject.text = text;
            weiboMessage.textObject = textObject;
            String image = data.has("image")?  data.getString("image"): "";
            Bitmap imageData = processImage(image);
            if (imageData != null) {
                //注意：最终压缩过的缩略图大小不得超过 32kb。
                ImageObject imageObject = new ImageObject();
                imageObject.setImageObject(imageData);
                weiboMessage.imageObject = imageObject;

            }
            // 2. 初始化从第三方到微博的消息请求
            SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
            // 用transaction唯一标识一个请求
            request.transaction = String.valueOf(System.currentTimeMillis());
            request.multiMessage = weiboMessage;
            // 3. 发送请求消息到微博，唤起微博分享界面
            AuthInfo authInfo = new AuthInfo(WeiboSDKPlugin.this.cordova.getActivity(), APP_KEY, REDIRECT_URL, SCOPE);
            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(WeiboSDKPlugin.this.cordova.getActivity());
            String token = "";
            if (accessToken != null) {
                token = accessToken.getToken();
            }
            mWeiboShareAPI.sendRequest(WeiboSDKPlugin.this.cordova.getActivity(), request, authInfo, token, new WeiboAuthListener() {

                @Override
                public void onWeiboException(WeiboException arg0) {
                    WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, WEIBO_EXCEPTION),
                        callbackContext.getCallbackId());
                }

                @Override
                public void onComplete(Bundle bundle) {
                    // TODO Auto-generated method stub
                    Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                    AccessTokenKeeper.writeAccessToken(WeiboSDKPlugin.this.cordova.getActivity(), newToken);
                    WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                        PluginResult.Status.OK), callbackContext.getCallbackId());
                }

                @Override
                public void onCancel() {
                    WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, CANCEL_BY_USER),
                        callbackContext.getCallbackId());
                }
            });
        } catch (JSONException e) {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, PARAM_ERROR),
                callbackContext.getCallbackId());
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
    /**
     * 处理图片
     * @param image
     * @return
     */
    private Bitmap processImage(String image) {
        if(URLUtil.isHttpUrl(image) || URLUtil.isHttpsUrl(image)) {
            return getBitmapFromURL(image);
        } else if (isBase64(image)) {
            return decodeBase64ToBitmap(image);
        } else {
            return getBitmapByPath(image);
        }
    }

    /**
     * 检查图片字符串是不是Base64
     * @param image
     * @return
     */
    private boolean isBase64(String image) {
        try {
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将图片的 URL 转化为 Bitmap
     * @param src
     * @return
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 根据文件路径生成 Bitmap
     * @param path
     * @return
     */
    public static Bitmap getBitmapByPath(String path) {
        if (path == null)
            return null;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (Error e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Base64解码成Bitmap
     */

    private Bitmap decodeBase64ToBitmap(String Base64String) {
        byte[] decode = Base64.decode(Base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        return bitmap;
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
                        WeiboSDKPlugin.this.cordova.getActivity(), mAccessToken);
                JSONObject jo = makeJson(mAccessToken.getToken(),
                        mAccessToken.getUid(),mAccessToken.getExpiresTime());
                WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                        PluginResult.Status.OK, jo), currentCallbackContext.getCallbackId());
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                // String code = values.getString("code");
                WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                                PluginResult.Status.ERROR, ONLY_GET_CODE),
                        currentCallbackContext.getCallbackId());
            }
        }

        @Override
        public void onCancel() {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, CANCEL_BY_USER),
                    currentCallbackContext.getCallbackId());
        }

        @Override
        public void onWeiboException(WeiboException e) {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                            PluginResult.Status.ERROR, WEIBO_EXCEPTION),
                    currentCallbackContext.getCallbackId());
        }
    };
}
