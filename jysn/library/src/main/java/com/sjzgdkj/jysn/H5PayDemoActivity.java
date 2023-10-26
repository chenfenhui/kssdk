package com.sjzgdkj.jysn;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class H5PayDemoActivity extends Activity {

	private WebView mWebView;
	public static H5PayDemoActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;
		Bundle extras = null;
		Log.i("msp", "8888");
		try {
			extras = getIntent().getExtras();
		} catch (Exception e) {
			finish();
			return;
		}
		Log.i("msp", "9999");
		if (extras == null) {
			finish();
			return;
		}
		Log.i("msp", "aaa");
		String url = null;
		try {
			url = extras.getString("url");
		} catch (Exception e) {
			finish();
			return;
		}
		if (TextUtils.isEmpty(url)) {
			// 测试H5支付，必须设置要打开的url网站
			Log.i("msp", "url is null");

		}
		Log.i("msp", "000");
		super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LinearLayout layout = new LinearLayout(getApplicationContext());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		setContentView(layout, params);
		Log.i("msp", "111");
		mWebView = new WebView(getApplicationContext());
		params.weight = 1;
		mWebView.setVisibility(View.VISIBLE);
		layout.addView(mWebView, params);
		Log.i("msp", "222");
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		Log.i("msp", "333");
		// 启用二方/三方 Cookie 存储和 DOM Storage
		// 注意：若要在实际 App 中使用，请先了解相关设置项细节。
		CookieManager.getInstance().setAcceptCookie(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
		}
		settings.setDomStorageEnabled(true);
		Log.i("msp", "444");
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.setWebViewClient(MainActivity.MyWebView);
		mWebView.loadUrl(url);

		Log.i("msp", "5555");
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			finish();
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWebView != null) {
			mWebView.removeAllViews();
			try {
				mWebView.destroy();
			} catch (Throwable t) {
			}
			mWebView = null;
		}
	}
}
