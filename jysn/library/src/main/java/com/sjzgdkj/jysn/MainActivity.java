package com.sjzgdkj.jysn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.sjzgdkj.jysn.wxapi.WXEntryActivity;
import com.sjzgdkj.jysn.wxapi.WXPayEntryActivity;import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Map;

public class MainActivity extends UnityPlayerActivity {


    //Appid final
    public static  String APP_ID ;

    public static  MyWebViewClient MyWebView ;
    //这个对象用于封装支付参数
    private PayReq req = new PayReq();
    //微信API 用于调起支付接口
    private IWXAPI wxAPI =null; //WXAPIFactory.createWXAPI(this, null);
    private String CallAliObjName;//CallAliObjName,CallAliFuncName
    private  String CallAliFuncName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("unity", "diandian.onCreate ");
        System.loadLibrary("msaoaidsec");
        MyWebView = new MyWebViewClient();
    }


    public void AliPayH5(String url) {
        Log.i("msp", "7777");
        Intent intent = new Intent(this, H5PayDemoActivity.class);
        Bundle extras = new Bundle();

        /*
         * URL 是要测试的网站，在 Demo App 中会使用 H5PayDemoActivity 内的 WebView 打开。
         *
         * 可以填写任一支持支付宝支付的网站（如淘宝或一号店），在网站中下订单并唤起支付宝；
         * 或者直接填写由支付宝文档提供的“网站 Demo”生成的订单地址
         * （如 https://mclient.alipay.com/h5Continue.htm?h5_route_token=303ff0894cd4dccf591b089761dexxxx）
         * 进行测试。
         *
         * H5PayDemoActivity 中的 MyWebViewClient.shouldOverrideUrlLoading() 实现了拦截 URL 唤起支付宝，
         * 可以参考它实现自定义的 URL 拦截逻辑。
         *
         * 注意：WebView 的 shouldOverrideUrlLoading(url) 无法拦截直接调用 open(url) 打开的第一个 url，
         * 所以直接设置 url = "https://mclient.alipay.com/cashier/mobilepay.htm......" 是无法完成网页转 Native 的。
         * 如果需要拦截直接打开的支付宝网页支付 URL，可改为使用 shouldInterceptRequest(view, request) 。
         */
        extras.putString("url", url);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                return true;
            }

            /**
             * 推荐采用的新的二合一接口(payInterceptorWithUrl),只需调用一次
             */
            final PayTask task = new PayTask(MainActivity.this);
            boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
                @Override
                public void onPayResult(final H5PayResultModel result) {
                    final String code = result.getResultCode();
                    final String url = result.getReturnUrl();
                    if (!TextUtils.isEmpty(url)) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                UnityPlayer.UnitySendMessage("WeChatComponent", "AliPayCallback", code);
                                view.loadUrl(url);
                                H5PayDemoActivity.instance.finish();
                            }
                        });
                    }
                }
            });

            /**
             * 判断是否成功拦截
             * 若成功拦截，则无需继续加载该URL；否则继续加载
             */
            if (!isIntercepted) {
                view.loadUrl(url);
            }
            return true;
        }
    }

    //微信SDK初始化(注册)的接口
    public void WechatInit(String appid){
        if(wxAPI == null) {
            this.APP_ID=appid;

            wxAPI = WXAPIFactory.createWXAPI(this, appid);
            wxAPI.registerApp(appid);
        }
    }

    //判断是否已经安装微信的接口
    public  boolean IsWechatInstalled () {
        return wxAPI.isWXAppInstalled();
    }

    //判断当前微信的版本是否支持API调用
    public  boolean IsWechatAppSupportAPI() {
        return true;
        //return wxAPI.isWXAppSupportAPI();
    }


    public void AliPay(final String orderInfo,String ObjName,String funName){
        final Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.this);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = 1;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    UnityPlayer.UnitySendMessage("WeChatComponent", "AliPayCallback", payResult.getResultStatus());
                    break;
                }
                default:
                    break;
            }
        };
    };

    public class PayResult {
        private String resultStatus;
        private String result;
        private String memo;

        public PayResult(Map<String, String> rawResult) {
            if (rawResult == null) {
                return;
            }

            for (String key : rawResult.keySet()) {
                if (TextUtils.equals(key, "resultStatus")) {
                    resultStatus = rawResult.get(key);
                } else if (TextUtils.equals(key, "result")) {
                    result = rawResult.get(key);
                } else if (TextUtils.equals(key, "memo")) {
                    memo = rawResult.get(key);
                }
            }
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo
                    + "};result={" + result + "}";
        }

        /**
         * @return the resultStatus
         */
        public String getResultStatus() {
            return resultStatus;
        }

        /**
         * @return the memo
         */
        public String getMemo() {
            return memo;
        }

        /**
         * @return the result
         */
        public String getResult() {
            return result;
        }
    }




    //微信登录的接口
    public  void LoginWechat(String appid,String state,String ObjName,String funName) {
        wxAPI.registerApp(APP_ID);
        Log.d("Unity","进入登录环节");
        WXEntryActivity.GameObjectName=ObjName;
        WXEntryActivity.CallBackFuncName=funName;
        // 发送授权登录信息，来获取code
        SendAuth.Req req = new SendAuth.Req();
        // 设置应用的作用域，获取个人信息msgApi  api

        req.scope = "snsapi_userinfo";
        req.state = state;
        wxAPI.sendReq(req);
    }

    public void GetDeviceIds(boolean isGetOAID,boolean isGetVAID,boolean isGetAAID,String ObjName,String funName) {
        DemoHelper demoHelper = new DemoHelper(ObjName,funName);
        demoHelper.getDeviceIds(this,isGetOAID,isGetVAID,isGetAAID);
    }

    public String GetDeviceIdLow() {
        TelephonyManager telManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telManager.getDeviceId();
        return deviceId;
    }

    public int GetDeviceVersion() {
        return Build.VERSION.SDK_INT;
    }
    //微信充值的接口
    public  void WeChatPayReq(String APP_ID,String MCH_ID ,String prepayid ,String noncestr,String timestamp,String sign,String callBackBackObjectName,String CallBackFuncName){
        //设置支付结果通知Unity的回调
        WXPayEntryActivity.GameObjectName = callBackBackObjectName;
        WXPayEntryActivity.CallBackFuncName = CallBackFuncName;
        //支付请求的参数
        req.appId = APP_ID;
        req.partnerId = MCH_ID;
        req.prepayId = prepayid;
        req.packageValue = "Sign=WXPay";
        req.nonceStr = noncestr;
        req.timeStamp = timestamp;
        req.sign = sign;
        //通过APPID校验应用
        //msgApi.registerApp(APP_ID);
        //这里是发起微信支付请求了
        wxAPI.sendReq(req);
    }

    //微信文字分享的接口
    public  void WXShareText(int shareType, String text,String objName,String funName) {

        WXEntryActivity.GameObjectName = objName;
        WXEntryActivity.CallBackFuncName = funName;//Unity层的回调

        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();

        req.transaction = BuildTransaction("text");
        req.message = msg;

        req.scene = shareType;
        wxAPI.sendReq(req);
    }

    //微信图片分享的接口
    public  void WXShareImage(int scene, byte[] imgData, byte[] thumbData,String objName,String funName) {

        WXEntryActivity.GameObjectName = objName;
        WXEntryActivity.CallBackFuncName = funName;//分享的物体名称和方法

        WXImageObject imgObj = new WXImageObject(imgData);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        msg.thumbData = thumbData;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = BuildTransaction("img");
        req.message = msg;
        req.scene = scene;
        wxAPI.sendReq(req);
    }


    //微信网页分享的接口
    public  void WXShareWebPage(int shareType, String url, String title, String content, byte[] thumb,String objName,String funName) {
        WXEntryActivity.GameObjectName = objName;
        WXEntryActivity.CallBackFuncName = funName;//Unity层的回调

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = content;
        msg.thumbData = thumb;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = BuildTransaction("webpage");
        req.message = msg;
        req.scene = shareType;
        wxAPI.sendReq(req);
    }

    static String BuildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

}