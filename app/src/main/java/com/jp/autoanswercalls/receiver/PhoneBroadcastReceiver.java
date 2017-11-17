package com.jp.autoanswercalls.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jp.autoanswercalls.service.PhoneService;

public class PhoneBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,PhoneService.class));
        //context.startService(new Intent(context, ServerSocketService.class));
    }
}
