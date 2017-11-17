package com.jp.autoanswercalls.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.baidu.aip.speech.AipSpeech;
import com.jp.autoanswercalls.utils.Constants;
import com.jp.autoanswercalls.utils.RecordAudioUtil;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;

public class AutoAnswerCallIntentService extends IntentService {

    public AutoAnswerCallIntentService() {
        super("AutoAnswerCallIntentService");
    }
    private MediaRecorder mRecorder = null;

    protected void onHandleIntent(Intent intent) {
        Context context = getBaseContext();
        SharedPreferences settings = context.getSharedPreferences(Constants.SETTING_PREF, 0);

        int state = ((TelephonyManager) context.getSystemService("phone")).getCallState();
        File file = null;
        Log.d(AutoAnswerCallIntentService.class.getName(), "Call state = " + state);
//        if (((TelephonyManager) context.getSystemService("phone")).getCallState() != 1) {
//            Log.d("AutoAnswer", "CALL_STATE_RINGING didn't detected!");
//            return;
//        }
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                try {
                    Log.d("AutoAnswer",""+ (mRecorder != null));
                    if (mRecorder != null) {
                        Log.d(AutoAnswerCallIntentService.class.getName(), "mRecorder.stop()");
                        mRecorder.stop(); // 停止
                        mRecorder.release();// 释放
                        mRecorder = null;// 垃圾回收
                    }
                    test(new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "/IncomeNumber_1.amr").getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                try {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);

                    file = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES), "/IncomeNumber_1.amr");
                    Log.d(AutoAnswerCallIntentService.class.getName(), "file.getAbsolutePath() = " + file.getAbsolutePath());
                    mRecorder.setOutputFile(file.getAbsolutePath());
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        mRecorder.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mRecorder.start();

                    Log.d(AutoAnswerCallIntentService.class.getName(), "mRecorder = " + mRecorder);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(AutoAnswerCallIntentService.class.getName(), "Phone receive an incoming call!");

                try {
                    Thread.sleep((long) (Integer.parseInt(settings.getString(Constants.ANSWER_CALL_DURATION, "1")) * 1000));
                    answerPhoneAidl(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static final String APP_ID = "10088675";
    public static final String API_KEY = "bI3hTV0NBQGW61RfWuW8C5kN";
    public static final String SECRET_KEY = "vtS1Yu2VlGUjPeMlQhG4lhtYAT8Xhivm";
    private void test(String path) {
        // 初始化一个FaceClient
        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        JSONObject asrRes = client.asr(path, "amr", 16000, null);
        Log.d(AutoAnswerCallIntentService.class.getName(), asrRes.toString());

        SocketClient client1 = new SocketClient("192.168.188.105",8081);
        String retStr = client1.sendMsg(asrRes.toString());
        Log.d(AutoAnswerCallIntentService.class.getName(), retStr);
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

}
