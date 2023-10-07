package com.ax.phonelisten;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Service;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.Context;

import com.unity3d.player.UnityPlayer;

public class MainActivity {

    private static MainActivity _instance;
    private Activity _activity;

    public static MainActivity instance(){
        if (_instance == null){
            //UnityPlayer.currentActivity表示当前正在显示的Unity Activity
            //作为context使用，例如toast函数。
            _instance = new MainActivity(UnityPlayer.currentActivity);
        }
        return _instance;
    }

    public MainActivity(Activity activity){
        _activity = activity;
    }


    public void AndoridPhoneListen()
    {

            TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //2、// CallProcess.HungUp(context, phoneNumber);	// 空闲/挂断处理逻辑
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //3、// CallProcess.OffHook(context, phoneNumber);	// 接听处理逻辑
                    break;

                case TelephonyManager.CALL_STATE_RINGING:        // 来电处理逻辑
                    //4、// CallProcess.Ringing(context, phoneNumber);
                    break;
            }
            UnityPlayer.UnitySendMessage("Global","AndroidPhoneListen",Integer.toString((state)));
        }
    };

    public boolean IsBluetoothHeadsetConnected() {
        AudioManager audioManager = (AudioManager) _activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    return true;
                }
            }
        }
        return false;
    }

}