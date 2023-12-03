package com.example.sweatbox_sms_test.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.example.sweatbox_sms_test.Interfaces.MyAPIService;
import com.example.sweatbox_sms_test.Models.UserModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_SMS_RECEIVED = "com.example.sweatbox_sms_test.ACTION_SMS_RECEIVED";
    private static final long DELAY_BETWEEN_MESSAGES = 15000;
    private static final String TAG = "SMSUtils";
//        private static final String API_URL = "http://10.0.2.2:3000/"; // for dev
    private static final String API_URL = "https://sweatbox-express.vercel.app/"; // for prod

    @Override
    public void onReceive(Context context, Intent intent) {
        String receivedAction = intent.getAction();
        Log.d(TAG, "onReceive: nineAmAlarm: " + intent.getBooleanExtra("nineAmAlarm", false));
        Log.d(TAG, "onReceive: threePmAlarm: " + intent.getBooleanExtra("threePmAlarm", false));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

//        nineAmAlarm(context, alarmManager);
//        threePmAlarm(context, alarmManager);
        if (intent.getBooleanExtra("nineAmAlarm", false)) {
            nineAmAlarm(context, alarmManager);
        }

        if (intent.getBooleanExtra("threePmAlarm", false)) {
            threePmAlarm(context, alarmManager);
        }

        if (receivedAction != null && receivedAction.equals(ACTION_SMS_RECEIVED)) {
            SmsManager smsManager = SmsManager.getDefault();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(100, TimeUnit.SECONDS)
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            MyAPIService myAPIService = retrofit.create(MyAPIService.class);
            sendSMS(smsManager, myAPIService);
        }
    }

    private void sendSMS(SmsManager smsManager, MyAPIService myAPIService) {
        JsonObject jsonObject = new JsonObject();
        JsonObject rangeValueMap = new JsonObject();

        Call<List<UserModel>> call = myAPIService.getAll();
        call.enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.code());
                }

                List<UserModel> userModels = response.body();

                TimerTask task = new TimerTask() {
                    int currentIndex = 0;

                    @Override
                    public void run() {
                        if (currentIndex < userModels.size()) {
                            try {
                                ArrayList<String> messageParts = smsManager.divideMessage(userModels.get(currentIndex).getMessage());
                                smsManager.sendMultipartTextMessage(userModels.get(currentIndex).getPhone_number(), null, messageParts, null, null);
                                rangeValueMap.addProperty(userModels.get(currentIndex).getColumn(), true);
                                Log.d(TAG, "SMS sent to: " + userModels.get(currentIndex).getPhone_number());
                            } catch (Exception e) {
                                Log.d(TAG, "Error sending SMS to: " + userModels.get(currentIndex).getPhone_number(), e);
                            }
                            currentIndex++;
                        } else {
                            if (rangeValueMap.size() > 0) {
                                jsonObject.add("rangeValueMap", rangeValueMap);
                                Log.d(TAG, "postData: " + jsonObject);
                                updateUserStatus(myAPIService, jsonObject);
                            }

                            cancel();
                        }
                    }
                };

                Timer timer = new Timer();
                timer.schedule(task, 0, DELAY_BETWEEN_MESSAGES);
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void updateUserStatus(MyAPIService myAPIService, JsonObject jsonArray) {
        Call<JsonObject> call = myAPIService.updateUserStatus(jsonArray);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "updateUserStatus: SUCCESSFUL " + response.body());
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d(TAG, "updateUserStatus: " + jObjError.getJSONObject("error").getString("message"));
                    } catch (Exception e) {
                        Log.d(TAG, "updateUserStatus: " + response.code() + response.errorBody());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "updateUserStatus onFailure: " + t.getMessage());
            }
        });
    }

    private void nineAmAlarm(Context context, AlarmManager alarmManager) {
        Intent intent = new Intent(context, SMSBroadcastReceiver.class);
        intent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        intent.putExtra("nineAmAlarm", "true");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        long nextAlarmTime = calendar.getTimeInMillis();
        Log.d(TAG, "nineAmAlarm nextAlarmTime: " + nextAlarmTime + " " + calendar.getTime());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
    }

    private void threePmAlarm(Context context, AlarmManager alarmManager) {
        Intent intent = new Intent(context, SMSBroadcastReceiver.class);
        intent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        intent.putExtra("threePmAlarm", "true");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        long nextAlarmTime = calendar.getTimeInMillis();
        Log.d(TAG, "threePmAlarm nextAlarmTime: " + nextAlarmTime + " " + calendar.getTime());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent);
    }
}
