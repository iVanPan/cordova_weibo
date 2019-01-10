package me.vanpan.weibosdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.webkit.URLUtil;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.WeiboAppManager;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAppInfo;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

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

public class WeiboSDKPlugin extends CordovaPlugin implements WbShareCallback {

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
    private static final String SHARE_FAIL ="sharefail";
    private static final String WEIBO_CLIENT_NOT_INSTALLED = "weibo client is not installed";
    public static CallbackContext currentCallbackContext;
    public static String APP_KEY;
    public static WbShareHandler shareHandler = null;
    private Oauth2AccessToken mAccessToken;
    private String REDIRECT_URL;
    private SsoHandler mSsoHandler;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        // The first letter "a" was added in plugin.xml to avoid the string be parsed as
        // a number, remove it here.
        APP_KEY = webView.getPreferences().getString(WEBIO_APP_ID, "a").substring(1);
        REDIRECT_URL = webView.getPreferences().getString(WEBIO_REDIRECT_URL, DEFAULT_URL);
        WbSdk.install(WeiboSDKPlugin.this.cordova.getActivity(),new AuthInfo(WeiboSDKPlugin.this.cordova.getActivity(), APP_KEY, REDIRECT_URL, SCOPE));
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
        } else if (action.equalsIgnoreCase("shareImageToWeibo")) {
            return shareImageToWeibo(callbackContext,args);
        } else if (action.equalsIgnoreCase("shareTextToWeibo")) {
            return shareTextToWeibo(callbackContext,args);
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
        mSsoHandler = new SsoHandler(WeiboSDKPlugin.this.cordova.getActivity());
        Runnable runnable = new Runnable() {
            public void run() {
                if (mSsoHandler != null) {
                    mSsoHandler.authorize(new SelfWbAuthListener());
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
        WbAppInfo wbAppInfo = WeiboAppManager.getInstance(WeiboSDKPlugin.this.cordova.getActivity()).getWbAppInfo();
        Boolean installed = (wbAppInfo != null && wbAppInfo.isLegal());
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
        mAccessToken = new Oauth2AccessToken();
        callbackContext.success();
        return true;
    }

    /**
     * 微博分享网页
     *
     * @param callbackContext
     * @param args
     * @return
     */
    private boolean shareToWeibo(final CallbackContext callbackContext,
                                 final CordovaArgs args) {
        currentCallbackContext = callbackContext;
        if (shareHandler == null) {
            shareHandler = new WbShareHandler(WeiboSDKPlugin.this.cordova.getActivity());
        }
        shareHandler.registerApp();
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                sendMultiMessage(callbackContext,args);
            }
        });
        return true;
    }

    /**
     * 微博图片分享
     * @param callbackContext
     * @param args
     * @return
     */
    private boolean shareImageToWeibo(final CallbackContext callbackContext,
        final CordovaArgs args) {
        currentCallbackContext = callbackContext;
        if (shareHandler == null) {
            shareHandler = new WbShareHandler(WeiboSDKPlugin.this.cordova.getActivity());
        }
        shareHandler.registerApp();
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                sendImageMessage(callbackContext,args);
            }
        });
        return true;
    }

    /**
     * 分享文字到微博
     * @param callbackContext
     * @param args
     * @return
     */
    private boolean shareTextToWeibo(final CallbackContext callbackContext,
        final CordovaArgs args) {
        currentCallbackContext = callbackContext;
        if (shareHandler == null) {
            shareHandler = new WbShareHandler(WeiboSDKPlugin.this.cordova.getActivity());
        }
        shareHandler.registerApp();
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                sendTextMessage(callbackContext,args);
            }
        });
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (mSsoHandler != null && requestCode == 32973) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, intent);
        } else if(requestCode == 1) {
            WeiboSDKPlugin.shareHandler.doResultIntent(intent,this);
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
            String title = data.has("title")?  data.getString("title"): "";
            String url = data.has("url")?  data.getString("url"): "";
            String description = data.has("description")?  data.getString("description"): "";
            String image = data.has("image")?  data.getString("image"): "";
            Bitmap imageData = processImage(image);
            //WebpageObject mediaObject = new WebpageObject();
            //mediaObject.identify = Utility.generateGUID();
            //mediaObject.title = title;
            //mediaObject.description = description;
            //mediaObject.actionUrl = url;
            if (imageData != null) {
                //注意：最终压缩过的缩略图大小不得超过 32kb。
                ImageObject imageObject = new ImageObject();
                imageObject.setImageObject(imageData);
                weiboMessage.imageObject = imageObject;
               // mediaObject.setThumbImage(imageData);
            }
           // weiboMessage.mediaObject = mediaObject;
            TextObject textObject = new TextObject();
            textObject.text = description + " " + url;
            textObject.title = title;
            weiboMessage.textObject = textObject;
            shareHandler.shareMessage(weiboMessage, false);
        } catch (JSONException e) {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, PARAM_ERROR),
                callbackContext.getCallbackId());
        }
    }

    /**
     * 组装图片分享消息
     * @param callbackContext
     * @param args
     */
    private void sendImageMessage(final CallbackContext callbackContext, CordovaArgs args) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        final JSONObject data;
        try {
            data = args.getJSONObject(0);
            String image = data.has("image")?  data.getString("image"): "";
            Bitmap imageData = processImage(image);
            if (imageData != null) {
                //注意：最终压缩过的缩略图大小不得超过 32kb。
                ImageObject imageObject = new ImageObject();
                imageObject.setImageObject(imageData);
                weiboMessage.imageObject = imageObject;
            }
            shareHandler.shareMessage(weiboMessage, false);
        } catch (JSONException e) {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, PARAM_ERROR),
                callbackContext.getCallbackId());
        }
    }

    /**
     * 组装微博文字分享消息
     * @param callbackContext
     * @param args
     */
    private void sendTextMessage(final CallbackContext callbackContext, CordovaArgs args) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        final JSONObject data;
        try {
            data = args.getJSONObject(0);
            String text = data.has("text")?  data.getString("text"): "";
            TextObject textObject = new TextObject();
            textObject.text = text;
            weiboMessage.textObject = textObject;
            shareHandler.shareMessage(weiboMessage, false);
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
     * @param userId
     * @param expires_time
     * @return
     */
    private JSONObject makeJson(String access_token, String userId, long expires_time) {
        String json = "{\"access_token\": \"" + access_token + "\", " +
            " \"userId\": \"" + userId
            + "\", " +
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

    @Override public void onWbShareSuccess() {
        WeiboSDKPlugin.currentCallbackContext.success();
    }

    @Override public void onWbShareCancel() {
        WeiboSDKPlugin.currentCallbackContext.error(CANCEL_BY_USER);
    }

    @Override public void onWbShareFail() {
        WeiboSDKPlugin.currentCallbackContext.error(SHARE_FAIL);
    }

    // @Override
    // public void onNewIntent(Intent intent) {
    //     super.onNewIntent(intent);
    //     WeiboSDKPlugin.shareHandler.doResultIntent(intent,this);
    // }

    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener{
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            mAccessToken = token;
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
        public void cancel() {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, CANCEL_BY_USER),
                currentCallbackContext.getCallbackId());
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            WeiboSDKPlugin.this.webView.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, WEIBO_EXCEPTION),
                currentCallbackContext.getCallbackId());
        }
    }
}
