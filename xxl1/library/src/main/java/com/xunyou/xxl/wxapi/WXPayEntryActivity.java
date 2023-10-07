package com.xunyou.xxl.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xunyou.xxl.MainActivity;
import com.unity3d.player.UnityPlayer;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";
    public static String GameObjectName = "GameObjectName";
    public static String CallBackFuncName = "WXPayCallback";
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建微信API对象
        api = WXAPIFactory.createWXAPI(this, MainActivity.APP_ID);
        //把接受到的Intent给wxapi这个对象，它会解析回调结果，通过我们实现的IWXAPIEventHandler接口回调给我们
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    //这里是核心:也就是微信支付结果会通知这个回调函数 在回调里我们通过UnityPlayer.UnitySendMessage通知Unity客户端支付结果
    @Override
    public void onResp(BaseResp resp) {
        String retCode = String.valueOf(resp.errCode);
        Toast.makeText(WXPayEntryActivity.this, "微信支付结果:"+retCode,
                Toast.LENGTH_SHORT).show();
        UnityPlayer.UnitySendMessage(GameObjectName,CallBackFuncName, retCode);
        finish();
        //Toast.makeText(WXPayEntryActivity.this, resp.errCode, Toast.LENGTH_SHORT).show();
		/*if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("微信支付结果" +String.valueOf(resp.errCode));
			builder.show();
		}*/

    }
}