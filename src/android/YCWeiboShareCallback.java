package org.zy.yuancheng.weibo;


import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class YCWeiboShareCallback extends Activity implements IWeiboHandler.Response {

	public static final String CANCEL_BY_USER ="cancel by user";
	public static final String SHARE_FAIL ="sharefail";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		YCWeibo.mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this,YCWeibo.APP_KEY);
		YCWeibo.mWeiboShareAPI.registerApp();
        YCWeibo.mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
	}

	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			YCWeibo.currentCallbackContext.success();
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			YCWeibo.currentCallbackContext.error(CANCEL_BY_USER);
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			YCWeibo.currentCallbackContext.error(SHARE_FAIL);
			break;
		}
		this.finish();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		YCWeibo.mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

}
