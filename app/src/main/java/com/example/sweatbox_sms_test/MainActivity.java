package com.example.sweatbox_sms_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.sweatbox_sms_test.Utils.SMSBroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    private static final int INTERVAL_FIFTEEN_MINUTES = 15 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleSMSBroadcast(this);
    }

    private void scheduleSMSBroadcast(Context context) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SMSBroadcastReceiver.class);
        intent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        long triggerTime = System.currentTimeMillis();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }
}