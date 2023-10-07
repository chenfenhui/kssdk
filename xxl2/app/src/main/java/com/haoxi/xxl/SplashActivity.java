package com.haoxi.xxl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.quicksdk.QuickSDK;
import com.quicksdk.QuickSdkSplashActivity;

public class SplashActivity extends QuickSdkSplashActivity {

	@Override
	public int getBackgroundColor() {
		return Color.WHITE;
	}

	@Override
	public void onSplashStop() {

		if (!this.isTaskRoot()) {
			Intent intent = getIntent();
			if (intent != null) {
				String action = intent.getAction();
				if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
					finish();
					return;
				}
			}
		}

		// 闪屏结束后，跳转到游戏界面
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		this.finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);



	}

}
