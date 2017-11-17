package com.jp.autoanswercalls.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.jp.autoanswercalls.utils.AudioRecordManager;
import com.jp.autoanswercalls.utils.PCMRecordAudioUtil;
import com.jp.autoanswercalls.utils.RecordAudioUtil;
import com.jp.autoanswercalls.utils.WavAudioUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * netstat -ano | findstr "5037"
 */
public class PhoneService extends Service {
    public static final String TAG = PhoneService.class.getName();
    private TelephonyManager telephonyManager = null;
    private RecordAudioUtil recordAudioUtil =null;

    private PCMRecordAudioUtil pcmRecordAudioUtil = null;
    private Intent intent = null;
    public PhoneService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() New Service was created");
        this.telephonyManager = (TelephonyManager) super.getSystemService(Context.TELEPHONY_SERVICE);
        this.telephonyManager.listen(new PhoneStateListenerImpl(), PhoneStateListener.LISTEN_CALL_STATE);
        Toast.makeText(this,"New Service was created",Toast.LENGTH_LONG).show();
        super.onCreate();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this,"Service Started",Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onStart() Service Started");
        super.onStart(intent, startId);
    }
    private class PhoneStateListenerImpl extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(TAG, "state = " + state);
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "Phone receive TelephonyManager.CALL_STATE_IDLE.");
                    if (PhoneService.this.recordAudioUtil != null){
                        PhoneService.this.recordAudioUtil.stop();
                        PhoneService.this.recordAudioUtil = null;
                    }
//                    if (null != PhoneService.this.pcmRecordAudioUtil) {
//                        PhoneService.this.pcmRecordAudioUtil.stopRecord();
//                        PhoneService.this.pcmRecordAudioUtil = null;
//                    }
//                    AudioRecordManager.getInstance().stopRecord();
//                    WavAudioUtil.getInstance().startRecord();
//                    WavAudioUtil.getInstance().convertWaveFile();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "Phone receive TelephonyManager.CALL_STATE_RINGING.");
//                    try {
//
//                        answerPhoneAidl(PhoneService.this.getBaseContext());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "Phone receive TelephonyManager.CALL_STATE_OFFHOOK.");

                    //获取音频服务
                    AudioManager audioManager = (AudioManager) PhoneService.this.getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                    //设置声音模式
                    audioManager.setMode(AudioManager.STREAM_MUSIC);
                    //关闭麦克风
                    audioManager.setMicrophoneMute(false);

                    PhoneService.this.recordAudioUtil = new RecordAudioUtil(incomingNumber,"incomingcall");
                    PhoneService.this.recordAudioUtil.record();

//                    PhoneService.this.pcmRecordAudioUtil = new PCMRecordAudioUtil();
//                    PhoneService.this.pcmRecordAudioUtil.startRecord();
//                    String outputPath = Environment.getExternalStorageDirectory().toString()+ File.separator + "CallRec" + File.separator+"CallRec_"+new SimpleDateFormat("yyyy"+"-"+"MM"+"-"+"dd"+"-"+
//                            "HH"+"-"+"mm"+"-"+"ss").format(new Date())+".pcm";
//                    AudioRecordManager.getInstance().startRecord(outputPath);
//                    WavAudioUtil.getInstance().startRecord();
//                    WavAudioUtil.getInstance().recordData();
                    break;
            }

            super.onCallStateChanged(state, incomingNumber);
        }
    }
    @Override
    public void onDestroy() {
        Toast.makeText(this,"Service destroyed",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void answerPhoneAidl(Context context) throws Exception {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService("phone");
            Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony", new Class[0]);
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(tm, new Object[0]);
            Log.d("AutoAnswer", "Phone answer incoming call now!");
            telephonyService.answerRingingCall();
        } catch (Exception e) {
            Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 79));
            context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
            intent = new Intent("android.intent.action.MEDIA_BUTTON");
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(1, 79));
            context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
        }
    }

    private void endPhoneAidl(Context context) throws Exception {
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService("phone");
            Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony", new Class[0]);
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(tm, new Object[0]);
            Log.d("AutoAnswer", "Phone endCall!");
            telephonyService.endCall();
        } catch (Exception e) {
            Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 79));
            context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
            intent = new Intent("android.intent.action.MEDIA_BUTTON");
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(1, 79));
            context.sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGED");
        }
    }
}
