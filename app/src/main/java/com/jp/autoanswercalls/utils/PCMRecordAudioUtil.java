package com.jp.autoanswercalls.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ESG020 on 2017/10/11.
 */

public class PCMRecordAudioUtil {

    public static final String TAG = "PCMRecordAudioUtil";

    //保存目录
    String recDir="CallRec";

    //pcm文件
    private File file;

    private File recAudioSaveFileDir;

    AudioRecord audioRecord;

    //是否在录制
    private boolean isRecording = false;

    private boolean sdCardExists = false;      //判断SD卡是否存在

    public PCMRecordAudioUtil() {
        if (this.sdCardExists = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //判断SD卡是否存在
            this.recAudioSaveFileDir = new File(Environment.getExternalStorageDirectory().toString()+File.separator+this.recDir+File.separator);    //保存录音目录
            if (!this.recAudioSaveFileDir.exists()){
                this.recAudioSaveFileDir.mkdir();           //如果父目录不存在创建目录.
            }
        }
    }

    //开始录音
    public void startRecord() {
        Log.i(TAG,"开始录音");
        //16K采集率
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        //16Bit
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        if (this.sdCardExists){
            String filePath = this.recAudioSaveFileDir.toString()+File.separator+"CallRec_"+new SimpleDateFormat("yyyy"+"-"+"MM"+"-"+"dd"+"-"+
                    "HH"+"-"+"mm"+"-"+"ss").format(new Date())+".pcm";
            //生成PCM文件
            file = new File(filePath);

            Log.i(TAG,"生成文件");
            //如果存在，就先删除再创建
            if (file.exists())
                file.delete();
            Log.i(TAG,"删除文件");
            try {
                file.createNewFile();
                Log.i(TAG,"创建文件");
            } catch (IOException e) {
                Log.i(TAG,"未能创建");
                throw new IllegalStateException("未能创建" + file.toString());
            }
            try {
                //输出流
                OutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_DOWNLINK, frequency, channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();
                Log.i(TAG, "开始录音");
                isRecording = true;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e(TAG, "录音失败");
            }
        }
    }

    public void stopRecord() {
        isRecording = false;

        this.audioRecord.stop();
        this.audioRecord.release();
    }
}
