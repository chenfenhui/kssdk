package com.haoxi.xxl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
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
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        Log.i("unity", "diandian.onCreate ");
        System.loadLibrary("msaoaidsec");

        QuickSDK.getInstance().setIsLandScape(true);

        QuickAllNotifier();
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

    public void QuickAllNotifier() {
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
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickInitSuc");
            }

            @Override
            public void onFailed(String message, String trace) {
                //初始化失败
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickInitFail");
            }
        });
    }

    public void QuickLoginNotifier() {
        Log.i("Unity","QuickLoginNotifier");
        QuickSDK.getInstance().setLoginNotifier(new LoginNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                //登录成功，获取到用户信息userInfo
                //通过userInfo中的UID、token做服务器登录认证
                Log.i("Unity","QuickLoginCallBack");
                Gson gson = new Gson();
                String json = gson.toJson(userInfo);
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickLoginCallBack", json);
            }

            @Override
            public void onCancel() {
                //登录取消
                Log.i("Unity","QuickLoginCancel");
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickLoginCancel");
            }

            @Override
            public void onFailed(final String message, String trace) {
                //登录失败
                Log.i("Unity","QuickLoginFailed");
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickLoginFailed");
            }
        });
    }

    public void QuickSwitchNotifier() {
        QuickSDK.getInstance().setSwitchAccountNotifier(new SwitchAccountNotifier() {
            @Override
            public void onSuccess(UserInfo userInfo) { //切换账号成功的回调，返回新账号的userInfo
                Gson gson = new Gson();
                String json = gson.toJson(userInfo);
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickSwitchCallBack", json);
            }

            @Override
            public void onCancel() {
//切换账号取消
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickSwitchSCancel");
            }

            @Override
            public void onFailed(String message, String trace) {
//切换账号失败
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickSwitchFailed");
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
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickPaySuc");
            }

            @Override
            public void onCancel(String cpOrderID) {
                //支付取消
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickPayCancel");
            }

            @Override
            public void onFailed(String cpOrderID, String message, String trace) {
                //支付失败
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickPayFailed");
            }
        });
    }

    public void QuickExitNotifier() {
        QuickSDK.getInstance().setExitNotifier(new ExitNotifier() {
            @Override
            public void onSuccess() {
                //退出成功，游戏在此做自身的退出逻辑处理
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickExitCancel");
            }

            @Override
            public void onFailed(String message, String trace) {
                //退出失败，不做处理
                UnityPlayer.UnitySendMessage("WeChatComponent", "QuickCallBack", "QuickExitFailed");
            }
        });
    }

    public void QuickInit(String code, String key) {
        Sdk.getInstance().init(this, code, key);
    }

    public void QuickLogin() {
        User.getInstance().login(this);
    }

    public void QuickRoelInfo(String name, String roleId, String ticket, String vip, boolean isCreate) {
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

    public void QuickPay(String name, String roleId, String ticket, String vip, String orderId,
                         String goodName, int GameCoin, double price, String goodId) {
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
}