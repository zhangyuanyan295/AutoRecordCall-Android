package com.jp.autoanswercalls.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jp.autoanswercalls.R;
import com.jp.autoanswercalls.utils.Constants;
import com.jp.autoanswercalls.utils.IPUtils;
import com.jp.autoanswercalls.utils.SDCardUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AnswerCallActivity extends Activity {
    public static String durationBeforeAnswerCall;
    public static boolean isAutoAnswerCallServiceEnabled;
    private CheckBox mCheckboxAutoAnswerCallEnableDisable;
    private EditText mDurationBeforeAnswerCallEditText;
    private TextView record_voice_path;
    private TextView ipAddress;
    private TextView receivedCmd;
    private static ArrayList<Socket> socketList = new ArrayList<Socket>();
    private ServerSocket serverSocket = null;
    private static Handler handler=new Handler();
    private BluetoothAdapter bluetoothAdapter;
    public AnswerCallActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.answer_call);
        record_voice_path = (TextView)findViewById(R.id.record_voice_path);
        ipAddress = (TextView)findViewById(R.id.ipAddress);
        receivedCmd = (TextView)findViewById(R.id.receivedCmd);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences.Editor editor = getSharedPreferences(Constants.SETTING_PREF, 0).edit();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  //判断SD卡是否存在
            record_voice_path.setText(SDCardUtil.getAbsoluteSdcardPath()+ File.separator+"EA_Call_Rec"+File.separator+"EA_Voice.amr");
        }

        ipAddress.setText(IPUtils.getIpAddress(this)+ ":4567");

        // 获得蓝牙适配器对象
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //this.startService(new Intent(this, ServerSocketService.class));
        new Thread(new ServerThread()).start();
    }

    public class ServerThread implements Runnable{

        private Socket socket = null;

        public ServerThread(){

        }

        public void run(){

            try{
                serverSocket = new ServerSocket(9000);
                while(true){
                    socket = serverSocket.accept();
                    socketList.add(socket);
                    new Thread(new HandleInputMessageThread(socket)).start();
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

//    通过adb的adb forward指令可以方便的通过USB连接建立PC端与Android的连接。
//            1. Android设备通过USB连接到计算机上，android手机打开开发者选项；
//            2. 确保手机连接到计算机的情况下，计算机上运行adb forward tcp:8000 tcp:9000，将PC端8000端口的数据, 转发到Android端的9000端口上；
//            3. Android设备上编写APP，作为网络通信的Server端，建立ServerSocket，打开端口9000；
//            4. PC机上编写程序，作为网络通信的Client端，打开端口8000；
//            5. PC端与Android端即可建立Socket连接进行数据通信。

//    public static void showRecv() throws UnknownHostException, IOException{
//        Socket socket = new Socket("127.0.0.1", 8000);
//        DataInputStream dis = new DataInputStream(socket.getInputStream());
//        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//        while(isLoop){
//            String textShow=dis.readUTF();
//            System.out.println(textShow);
//        }
//        socket.close();
//    }

    public class HandleInputMessageThread implements Runnable{
        private Socket socket = null;

        public HandleInputMessageThread(Socket sock){
            this.socket = sock;
        }

        public void run(){
            while(true){
                InputStream inputStream = null;
                try{
                    inputStream = socket.getInputStream();
                    byte data[] = new byte[1024*4];
                    int i = 0;
                    while((i = inputStream.read(data))!=-1){
                        String buffer = null;
                        buffer = new String(data,"gbk");

                        final String temp = buffer.trim();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                receivedCmd.setText("RECEIVED CMD = "+temp);
                                boolean blueToothState = false;
                                if(temp.equals("BT_ON")) {
                                    blueToothState = bluetoothAdapter.isEnabled();
                                    if (blueToothState) {
                                        Toast.makeText(AnswerCallActivity.this, "蓝牙已经打开", Toast.LENGTH_SHORT).show();
                                    } else {
                                        bluetoothAdapter.enable();
                                        Toast.makeText(AnswerCallActivity.this, "正在打开..", Toast.LENGTH_SHORT).show();
                                    }
                                } else if(temp.equals("BT_OFF")) {
                                    // 判断蓝牙的状态
                                    blueToothState = bluetoothAdapter.isEnabled();
                                    if (blueToothState) {
                                        bluetoothAdapter.disable();
                                        Toast.makeText(AnswerCallActivity.this, "正在关闭.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AnswerCallActivity.this, "蓝牙已经关闭!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        OutputStream outputStream = null;

                        for(Socket sock : socketList){
                            outputStream = sock.getOutputStream();
                            outputStream.write(buffer.getBytes("gbk"));
                            outputStream.flush();
                        }
                        break;
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
