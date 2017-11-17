package com.jp.autoanswercalls.utils;

import android.util.Base64;
import android.util.Log;

import com.baidu.aip.util.Base64Util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ESG020 on 2017/9/5.
 */

public class BaiduVoiceUtil {

    private static final String serverURL = "http://vop.baidu.com/server_api";
    private static String token = "";
    private static final String testFileName = "test.pcm";
    //put your own params here
    private static final String apiKey = "bI3hTV0NBQGW61RfWuW8C5kN";
    private static final String secretKey = "vtS1Yu2VlGUjPeMlQhG4lhtYAT8Xhivm";
    private static final String cuid = "10088675";

    public static void getToken() throws Exception {
        String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials" +
                "&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL).openConnection();
        token = new JSONObject(printResponse(conn)).getString("access_token");
    }

    public static String recognize(String amrFilePath) throws Exception {
        File amrFile = new File(amrFilePath);
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();

        // construct params
        JSONObject params = new JSONObject();
        params.put("format", "amr");
        params.put("rate", 8000);
        params.put("channel", "1");
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("len", amrFile.length());
//        params.put("speech", DatatypeConverter.printBase64Binary(loadFile(pcmFile)));
        //params.put("speech", Base64.encodeToString(loadFile(amrFile), Base64.DEFAULT));
        params.put("speech", Base64Util.encode(loadFile(amrFile)));

        // add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        conn.setDoInput(true);
        conn.setDoOutput(true);

        // send request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(params.toString());
        wr.flush();
        wr.close();

        return printResponse(conn);
    }

    private static String printResponse(HttpURLConnection conn) throws Exception {
        if (conn.getResponseCode() != 200) {
            // request error
        }
        InputStream is = conn.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        //System.out.println(new JSONObject(response.toString()).toString(4));
        return response.toString();
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    public static String miniRecognize(String amrFilePath) {
        try {
            getToken();
            return recognize(amrFilePath);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
