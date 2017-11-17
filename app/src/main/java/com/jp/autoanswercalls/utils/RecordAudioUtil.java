package com.jp.autoanswercalls.utils;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Jason on 12/11/2015.
 */
//录音工具类
public class RecordAudioUtil {
    private MediaRecorder mediaRecorder=null; //媒体录制
    String recDir="EA_Call_Rec";                  //保存目录
    private File recAudioSaveFileDir =null;   //文件保存目录
    private boolean sdCardExists = false;      //判断SD卡是否存在
    private boolean isRec=false;                //判断录音状态
    private String phoneNumber = null;          //记录号码
    private String callType = null;             //记录拨入或拨出类型

    public RecordAudioUtil(String callType, String phoneNumber) {
        this.callType = callType;
        this.phoneNumber = phoneNumber;
        if (this.sdCardExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //判断SD卡是否存在
            this.recAudioSaveFileDir = new File(Environment.getExternalStorageDirectory().toString()+File.separator+this.recDir+File.separator);    //保存录音目录
            if (!this.recAudioSaveFileDir.exists()){
                this.recAudioSaveFileDir.mkdir();           //如果父目录不存在创建目录.
            }
        }
    }

    String recAudioSaveFileName = null;
    public File record(){
        File recAudioSaveFile = null;

        if (this.sdCardExists){
//            recAudioSaveFileName = this.recAudioSaveFileDir.toString()+File.separator+"CallRec_"+new SimpleDateFormat("yyyy"+"-"+"MM"+"-"+"dd"+"-"+
//            "HH"+"-"+"mm"+"-"+"ss").format(new Date())+"_"+this.callType+"_"+this.phoneNumber+".amr";

            recAudioSaveFileName = this.recAudioSaveFileDir.toString()+File.separator+"EA_Voice.amr";

            Log.d(RecordAudioUtil.class.getName(), "Start:" + recAudioSaveFileName);

            recAudioSaveFile = new File(recAudioSaveFileName);

            if (recAudioSaveFile.exists()) {
                Log.d(RecordAudioUtil.class.getName(), "recAudioSaveFile.exists()");
                recAudioSaveFile.delete();
            }
            this.mediaRecorder = new MediaRecorder();
//            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
//            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
            this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            this.mediaRecorder.setAudioEncodingBitRate(4750);
            this.mediaRecorder.setAudioChannels(1);
            this.mediaRecorder.setAudioSamplingRate(8000);

            this.mediaRecorder.setOutputFile(recAudioSaveFile.getAbsolutePath());
            try {
                this.mediaRecorder.prepare();
                this.mediaRecorder.start();
                this.isRec = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return recAudioSaveFile;
    }
    public void stop(){
        if (this.isRec){
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            Log.d(RecordAudioUtil.class.getName(), "Stop: "+recAudioSaveFileName);

            //new Thread(networkTask).start();
        }
    }

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            String responseJson = BaiduVoiceUtil.miniRecognize(recAudioSaveFileName);
            Log.d(RecordAudioUtil.class.getName(), responseJson);

//            SocketClient client = new SocketClient("192.168.1.101", 6789);
//            try {
//                JSONObject obj = new JSONObject(responseJson.toString());
//                String retStr = client.sendMsg(obj.getJSONArray("result").getString(0).toString());
//                Log.d(RecordAudioUtil.class.getName(), retStr);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

        }
    };
}
