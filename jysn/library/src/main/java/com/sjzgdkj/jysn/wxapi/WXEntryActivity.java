package com.sjzgdkj.jysn.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.sjzgdkj.jysn.MainActivity;
import com.unity3d.player.UnityPlayer;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;
    public static String GameObjectName = "GameObjectName";
    public static String CallBackFuncName = "WXPayCallback";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (api == null) {
            api = WXAPIFactory.createWXAPI(this,  MainActivity.APP_ID);
            api.handleIntent(getIntent(), this);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {

    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        Log.i("Unity", resp.getType()+ "====" +  resp.errCode);
        switch (resp.getType()){
            case 1://授权den
                //ErrCode	ERR_OK = 0(用户同意) ERR_AUTH_DENIED = -4（用户拒绝授权） ERR_USER_CANCEL = -2（用户取消）
                if(resp.errCode == BaseResp.ErrCode.ERR_OK){
                    //(SendAuth.Resp) resp).code 返回给Unity客户端 去获取OpenID
                    Toast.makeText(WXEntryActivity.this, "微信登录成功",
                            Toast.LENGTH_SHORT).show();
                    UnityPlayer.UnitySendMessage(GameObjectName,CallBackFuncName, ((SendAuth.Resp) resp).code);
                }else{
                    if(resp.errCode == BaseResp.ErrCode.ERR_AUTH_DENIED){
                        Toast.makeText(WXEntryActivity.this, "用户拒绝授权",
                                Toast.LENGTH_SHORT).show();
                        UnityPlayer.UnitySendMessage(GameObjectName,CallBackFuncName, "用户拒绝授权");
                    }
                    else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL){
                        Toast.makeText(WXEntryActivity.this, "用户取消授权",
                                Toast.LENGTH_SHORT).show();
                        UnityPlayer.UnitySendMessage(GameObjectName,CallBackFuncName, "用户取消授权");
                    }
                }
                break;
            case 2://分享
                Toast.makeText(WXEntryActivity.this, "分享结果:"+resp.errCode,
                        Toast.LENGTH_SHORT).show();
                UnityPlayer.UnitySendMessage(GameObjectName,CallBackFuncName, "" + resp.errCode);
                break;
        }
        finish();
    }
}