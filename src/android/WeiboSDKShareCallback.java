package me.vanpan.weibosdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

public class WeiboSDKShareCallback extends Activity implements IWeiboHandler.Response {

	private static final String CANCEL_BY_USER ="cancel by user";
	private static final String SHARE_FAIL ="sharefail";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WeiboSDKPlugin.mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,WeiboSDKPlugin.APP_KEY);
		WeiboSDKPlugin.mWeiboShareAPI.registerApp();
		WeiboSDKPlugin.mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
	}

	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			WeiboSDKPlugin.currentCallbackContext.success();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			WeiboSDKPlugin.currentCallbackContext.error(CANCEL_BY_USER);
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			WeiboSDKPlugin.currentCallbackContext.error(SHARE_FAIL);
			break;
		}
		this.finish();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		WeiboSDKPlugin.mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

}
