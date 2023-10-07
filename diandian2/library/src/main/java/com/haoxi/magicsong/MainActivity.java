package com.haoxi.magicsong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.netease.mobsec.GetTokenCallback;
import com.netease.mobsec.InitCallback;
import com.netease.mobsec.WatchMan;
import com.netease.mobsec.WatchManConf;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.haoxi.magicsong.wxapi.WXEntryActivity;
import com.haoxi.magicsong.wxapi.WXPayEntryActivity;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Map;

public class MainActivity extends UnityPlayerActivity {


    //Appid final
    public static  String APP_ID ;

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

        WatchManConf myconf = new WatchManConf();
        myconf.setCollectApk(true);		// 采集apk安装列表
        myconf.setCollectSensor(false);	//init时候不采集传感器数据

        //初始化SDK
        WatchMan.init(this, "YD00340755267720", myconf, new InitCallback() {
            @Override
            public void onResult(int code, String msg) {
                Log.e("Unity","init OnResult , code = " + code + " msg = " + msg);
            }
        });
        //setContentView(R.layout.activity_main);
//        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
//                || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
//            //没有 ，  申请权限  权限数组
//            Log.i("unity", "申请权限: ");
//            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_PHONE_STATE ,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//        else
//        {
//            Log.i("unity", "已获得权限: ");
//        }
    }

    public void GetYIDUNToken(String ObjName,String funName) {

        WatchMan.setSeniorCollectStatus(true);  // 开启传感器数据采集

        // 获取反作弊查询token,使用默认延时3秒
        WatchMan.getTokenAsync(3000, new GetTokenCallback(){
            @Override
            public void onResult(int code, String msg,String Token) {
                Log.e("Unity","Register, code = " + code + " msg = " + msg+" Token:"+Token);
                UnityPlayer.UnitySendMessage(ObjName,funName, Token);
            }
        });

        WatchMan.setSeniorCollectStatus(false); //提交token后，关闭传感器数据采集
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Log.i("unity", "获得权限: ");
//        }else{
//
//        }
//    }

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