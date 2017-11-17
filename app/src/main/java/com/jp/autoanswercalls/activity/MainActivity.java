package com.jp.autoanswercalls.activity;

import android.app.Activity;
import android.os.Bundle;

import com.android.internal.telephony.ITelephony;
import com.jp.autoanswercalls.R;

public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private ITelephony mTelephonyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}