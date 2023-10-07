package com.haoxi.xxl;

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
import android.widget.Toast;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.bytedance.applog.AppLog;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.game.GameReportHelper;
import com.bytedance.applog.util.UriConstants;
import com.bytedance.hume.readapk.HumeSDK;
import com.google.gson.Gson;
import com.haoxi.xxl.utils.AuthResult;
import com.haoxi.xxl.utils.OrderInfoUtil2_0;
import com.haoxi.xxl.wxapi.WXEntryActivity;
import com.haoxi.xxl.wxapi.WXPayEntryActivity;
import com.kwai.monitor.log.TurboAgent;
import com.kwai.monitor.log.TurboConfig;
import com.kwai.monitor.payload.TurboHelper;
import com.netease.mobsec.GetTokenCallback;
import com.netease.mobsec.InitCallback;
import com.netease.mobsec.WatchMan;
import com.netease.mobsec.WatchManConf;
import com.quicksdk.Payment;
import com.quicksdk.QuickSDK;
import com.quicksdk.Sdk;
import com.quicksdk.User;
import com.quicksdk.entity.GameRoleInfo;
import com.quicksdk.entity.OrderInfo;
import com.quicksdk.entity.UserInfo;
import com.quicksdk.notifier.ExitNotifier;
import com.quicksdk.notifier.InitNotifier;
import com.quicksdk.notifier.LoginNotifier;
import com.quicksdk.notifier.PayNotifier;
import com.quicksdk.notifier.SwitchAccountNotifier;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
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

        QuickSDK.getInstance().setIsLandScape(true);

        QuickInitNotifier();
        QuickInit("89853707354860538270900980335533", "42870954");
        com.quicksdk.Sdk.getInstance().onCreate(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        com.quicksdk.Sdk.getInstance().onStart(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        com.quicksdk.Sdk.getInstance().onRestart(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        com.quicksdk.Sdk.getInstance().onPause(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        com.quicksdk.Sdk.getInstance().onResume(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        com.quicksdk.Sdk.getInstance().onStop(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        com.quicksdk.Sdk.getInstance().onDestroy(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        com.quicksdk.Sdk.getInstance().onNewIntent(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        com.quicksdk.Sdk.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }

    public  void QuickAllNotifier()
    {
        QuickInitNotifier();
        QuickLoginNotifier();
        QuickSwitchNotifier();
        QuickPayNotifier();
        QuickExitNotifier();
    }

    public void QuickInitNotifier() {
        QuickSDK.getInstance().setInitNotifier(new InitNotifier() {
            @Override
            public void onSuccess() {
                //初始化成功
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickInitSuc");
            }
            @Override
            public void onFailed(String message, String trace) {
                //初始化失败
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickInitFail");
            } });
    }

    public void QuickLoginNotifier() {
        QuickSDK.getInstance().setLoginNotifier(new LoginNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                //登录成功，获取到用户信息userInfo
                //通过userInfo中的UID、token做服务器登录认证
                Gson gson = new Gson();
                String json = gson.toJson(userInfo);
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickLoginCallBack", json);
            }
            @Override
            public void onCancel() {
                //登录取消
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickLoginCancel");
            }
            @Override
            public void onFailed(final String message, String trace) {
                //登录失败
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickLoginFailed");
            }
        });
    }

    public void QuickSwitchNotifier() {
        QuickSDK.getInstance().setSwitchAccountNotifier(new SwitchAccountNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) { //切换账号成功的回调，返回新账号的userInfo
                Gson gson = new Gson();
                String json = gson.toJson(userInfo);
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickSwitchCallBack", json);
            }

            @Override
            public void onCancel() {
//切换账号取消
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickSwitchSCancel");
            }
            @Override
            public void onFailed(String message, String trace) {
//切换账号失败
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickSwitchFailed");
            }
        });
    }

    public void QuickPayNotifier() {
        QuickSDK.getInstance().setPayNotifier(new PayNotifier() {
            @Override
            public void onSuccess(String sdkOrderID, String cpOrderID,
                                  String extrasParams) {
                //支付成功
                //sdkOrderID:quick订单号 cpOrderID：游戏订单号
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickPaySuc");
            }
            @Override
            public void onCancel(String cpOrderID) {
                //支付取消
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickPayCancel");
            }
            @Override
            public void onFailed(String cpOrderID, String message, String trace) {
                //支付失败
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickPayFailed");
            }
        });
    }

    public void QuickExitNotifier() {
        QuickSDK.getInstance().setExitNotifier(new ExitNotifier() {
            @Override
            public void onSuccess() {
                //退出成功，游戏在此做自身的退出逻辑处理
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickExitCancel");
            }
            @Override
            public void onFailed(String message, String trace) {
                //退出失败，不做处理
                UnityPlayer.UnitySendMessage("WeChatComponent","QuickCallBack", "QuickExitFailed");
            }
        });
    }

    public void QuickInit(String code,String key) {
        Sdk.getInstance().init(this, code, key);
    }

    public void QuickLogin() {
        User.getInstance().login(this);
    }

    public  void QuickRoelInfo(String name,String roleId,String ticket,String vip,boolean isCreate)
    {
        GameRoleInfo roleInfo = new GameRoleInfo();
        roleInfo.setServerID("10001");//数字字符串，不能含有中文字符
        roleInfo.setServerName("默认");
        roleInfo.setGameRoleName(name);
        roleInfo.setGameRoleID(roleId);
        roleInfo.setGameBalance(ticket);
        roleInfo.setVipLevel(vip);//设置当前用户vip等级，必须为数字整型字符串,请勿传"vip1"等类似字符串
        roleInfo.setGameUserLevel("0");//设置游戏角色等级
        roleInfo.setPartyName("0");//设置帮派名称
        roleInfo.setRoleCreateTime("0000000000"); //值为10位数时间戳
        roleInfo.setPartyId("0");//设置帮派id，必须为整型字符串
        roleInfo.setGameRoleGender("0");
        roleInfo.setGameRolePower("0"); //设置角色战力，必须为整型字符串
        roleInfo.setPartyRoleId("0"); //设置角色在帮派中的id
        roleInfo.setPartyRoleName("0"); //设置角色在帮派中的名称
        roleInfo.setProfessionId("0"); //设置角色职业id，必须为整型字符串
        roleInfo.setProfession("0"); //设置角色职业名称
        roleInfo.setFriendlist("0");

        User.getInstance().setGameRoleInfo(this, roleInfo, isCreate);
    }

    public  void QuickPay(String name,String roleId,String ticket,String vip,String orderId,
                          String goodName,int GameCoin,double price,String goodId)
    {
        GameRoleInfo roleInfo = new GameRoleInfo();
        roleInfo.setServerID("10001");//数字字符串，不能含有中文字符
        roleInfo.setServerName("默认");
        roleInfo.setGameRoleName(name);
        roleInfo.setGameRoleID(roleId);
        roleInfo.setGameBalance(ticket);
        roleInfo.setVipLevel(vip);//设置当前用户vip等级，必须为数字整型字符串,请勿传"vip1"等类似字符串
        roleInfo.setGameUserLevel("0");//设置游戏角色等级
        roleInfo.setPartyName("0");//设置帮派名称
        roleInfo.setRoleCreateTime("0000000000"); //值为10位数时间戳
        roleInfo.setPartyId("0"); //设置帮派id，必须为整型字符串
        roleInfo.setGameRoleGender("0");
        roleInfo.setGameRolePower("0"); //设置角色战力，必须为整型字符串
        roleInfo.setPartyRoleId("0"); //设置角色在帮派中的id
        roleInfo.setPartyRoleName("0"); //设置角色在帮派中的名称
        roleInfo.setProfessionId("0"); //设置角色职业id，必须为整型字符串
        roleInfo.setProfession("0"); //设置角色职业名称
        roleInfo.setFriendlist("0");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCpOrderID(orderId);
        orderInfo.setGoodsName(goodName);//商品名称，不带数量
        orderInfo.setCount(GameCoin);//游戏币数量
        orderInfo.setAmount(price);
        orderInfo.setPrice(price);
        orderInfo.setGoodsID(goodId);
        orderInfo.setGoodsDesc(goodName);
        orderInfo.setExtrasParams("0");

        Payment.getInstance().pay(this, orderInfo, roleInfo);
    }

    public void QuickLogout() {
        User.getInstance().logout(this);
    }

    public void QuickExit() {
        Sdk.getInstance().exit(this);
    }
    public void ksAppActive() {
        TurboAgent.onAppActive();
    }

    public void ksRegister() {
        TurboAgent.onRegister();
    }

    public void ksPay(double money) {
        //3.付费成功时调用，参数为价格，单位：元
        TurboAgent.onPay(money);
    }

    public void ksInit(String appId,String appName,String appChannel) {
        TurboAgent.init(TurboConfig.TurboConfigBuilder.create(this)
                .setAppId(appId)
                .setAppName(appName)
                .setAppChannel(appChannel)
                .setEnableDebug(true)
                .build());
    }

    public void ksResume() {
        TurboAgent.onPageResume(this);
    }

    public void ksPause() {
        TurboAgent.onPagePause(this);
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

    public String GetKuaishouChannel() {
        return TurboHelper.getChannel(this.getApplicationContext());
    }

    public String GetBytesChannel() {
        return HumeSDK.getChannel(this.getApplicationContext());
    }

    public void InitBytesSDK(String appId,String channel,boolean isLog,boolean isCompress) {
        /* 初始化SDK开始 */
        // 第一个参数APPID: 参考2.1节获取
        // 第二个参数CHANNEL: 填写渠道信息，请注意不能为空
        final InitConfig config = new InitConfig(appId, channel);
        // 设置数据上送地址
        config.setUriConfig(UriConstants.DEFAULT);
        config.setImeiEnable(false);//建议关停获取IMEI（出于合规考虑）
        config.setAutoTrackEnabled(true); // 全埋点开关，true开启，false关闭
        config.setLogEnable(isLog); // true:开启日志，参考4.3节设置logger，false:关闭日志
        config.setEnablePlay(true);
        AppLog.setEncryptAndCompress(isCompress); // 加密开关，true开启，false关闭
        AppLog.init(this, config);
    }

    public void onEventRegister(String type,boolean isSuc) {
        GameReportHelper.onEventRegister(type,isSuc);
    }

    public void onEventPurchase(String type,String name,String id,int num,String payType,String coinType, boolean isSuc,int coinNum) {
        GameReportHelper.onEventPurchase(type,name, id,num, payType,coinType, isSuc, coinNum);
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
                case 2: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        UnityPlayer.UnitySendMessage("WeChatComponent", "AliAuthCallback", authResult.getAuthCode());
                    } else {
                        // 其他状态值则为授权失败
                        Log.i("Unity", "auth fail");
                    }
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

    public String GetShareParam(String TIMESteamp,String APPID,String RSA2_PRIVATE,String Token)
    {
        Map<String, String> authInfoMap = OrderInfoUtil2_0.buildShareInfoMap(APPID, TIMESteamp, Token);
        String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);

        String sign = OrderInfoUtil2_0.getSign(authInfoMap, RSA2_PRIVATE, true);
        final String authInfo = info + "&" + sign;

        return authInfo;
    }

    public String GetTokenParam(String TIMESteamp,String APPID,String RSA2_PRIVATE,String Token)
    {
        Map<String, String> authInfoMap = OrderInfoUtil2_0.buildTokenInfoMap(APPID, TIMESteamp, Token);
        String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);

        String sign = OrderInfoUtil2_0.getSign(authInfoMap, RSA2_PRIVATE, true);
        final String authInfo = info + "&" + sign;

        return authInfo;
    }


    /**
     * 支付宝账户授权业务示例
     */
    public void authV2(String PID,String APPID,String RSA2_PRIVATE,String TARGET_ID) {
        if (TextUtils.isEmpty(PID) || TextUtils.isEmpty(APPID)
                || (TextUtils.isEmpty(RSA2_PRIVATE))
                || TextUtils.isEmpty(TARGET_ID)) {
            Toast.makeText(MainActivity.this, "param is null",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * authInfo 的获取必须来自服务端；
         */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
        Map<String, String> authInfoMap = OrderInfoUtil2_0.buildAuthInfoMap(PID, APPID, TARGET_ID, rsa2);
        String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);

        String sign = OrderInfoUtil2_0.getSign(authInfoMap, RSA2_PRIVATE, rsa2);
        final String authInfo = info + "&" + sign;
        Runnable authRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造AuthTask 对象
                AuthTask authTask = new AuthTask(MainActivity.this);
                // 调用授权接口，获取授权结果
                Map<String, String> result = authTask.authV2(authInfo, true);

                Message msg = new Message();
                msg.what = 2;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread authThread = new Thread(authRunnable);
        authThread.start();
    }

}

