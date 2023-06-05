package com.example.sweatbox_sms_test.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.sweatbox_sms_test.Utils.SMSBroadcastReceiver;

public class SMSSenderService extends Service {
    private static final int INTERVAL = 2 * 60 * 1000;
    private static final String TAG = "SMSSenderService";
    private SMSBroadcastReceiver smsBroadcastReceiver;
    private PendingIntent pendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
