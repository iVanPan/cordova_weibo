package org.zy.yuancheng.weibo;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import android.content.Intent;
import android.os.Bundle;


public class YCWeibo extends CordovaPlugin {

	private static final String TAG = YCWeibo.class.getSimpleName();
	    // 新支持scope：支持传入多个scope权限，用逗号分隔
	 public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

	public String callbackId="";
	private AuthInfo mAuthInfo;
    
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;

	private SsoHandler mSsoHandler;

	@Override
	public boolean execute(String action, final JSONArray args,final CallbackContext callbackContext) throws JSONException {
		// TODO Auto-generated method stub
		boolean result=false;
		String APP_KEY = webView.getProperty("weibo_app_id", "");
		String REDIRECT_URL=webView.getProperty("redirecturi", "");
		final PluginResult pr = new PluginResult(PluginResult.Status.NO_RESULT);
		mAuthInfo = new AuthInfo(YCWeibo.this.cordova.getActivity(), APP_KEY,REDIRECT_URL,SCOPE);
        mSsoHandler = new SsoHandler(YCWeibo.this.cordova.getActivity(), mAuthInfo);
		pr.setKeepCallback(true);
		callbackId=callbackContext.getCallbackId();
		mAccessToken = AccessTokenKeeper.readAccessToken(YCWeibo.this.cordova.getActivity());
		if (action.equals("ssoLogin")) {
			 if (mAccessToken.isSessionValid()) {
				 JSONObject jo = makeJson(mAccessToken.getToken(), mAccessToken.getUid());
				 this.webView.sendPluginResult(new PluginResult(PluginResult.Status.OK, jo), this.callbackId);
		         result=true;
		        } else {
		        	Runnable runnable = new Runnable(){
						public void run() {
							if(mSsoHandler!=null){
								mSsoHandler.authorize(new AuthListener(YCWeibo.this));
							}
							};
					};
					this.cordova.setActivityResultCallback(this);
					this.cordova.getActivity().runOnUiThread(runnable);
					result=true;
		        }
		}
		if(action.equals("logout")){
			AccessTokenKeeper.clear(this.cordova.getActivity());
			callbackContext.success();
			result=true;	
		}

		return result;
	}

	 class AuthListener implements WeiboAuthListener {
		 final YCWeibo mWeibo;
			public AuthListener(YCWeibo weibo) {
				super();
				this.mWeibo = weibo;
			}
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(YCWeibo.this.cordova.getActivity(), mAccessToken);
                JSONObject jo = makeJson(mAccessToken.getToken(), mAccessToken.getUid());			
    			this.mWeibo.webView.sendPluginResult(new PluginResult(PluginResult.Status.OK, jo), this.mWeibo.callbackId);
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
    			this.mWeibo.webView.sendPluginResult(new PluginResult(PluginResult.Status.ERROR), this.mWeibo.callbackId);

            }
        }

        @Override
        public void onCancel() {
			this.mWeibo.webView.sendPluginResult(new PluginResult(PluginResult.Status.ERROR), this.mWeibo.callbackId);
        }

        @Override
        public void onWeiboException(WeiboException e) {
			this.mWeibo.webView.sendPluginResult(new PluginResult(PluginResult.Status.ERROR), this.mWeibo.callbackId);  
        }
    }
	/**
	 * 组装JSON
	 * @param access_token
	 * @param uid
	 * @return
	 */
	private JSONObject makeJson(String access_token,String userid){
		String json = "{\"access_token\": \"" + access_token
				+ "\",  \"userid\": \"" + userid+"\"}";
		JSONObject jo = null;
		try {
			jo = new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, intent);
		if(mSsoHandler!=null){
			mSsoHandler.authorizeCallBack(requestCode, resultCode, intent);
		}

	}
	
	
}