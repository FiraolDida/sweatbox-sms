package com.example.sweatbox_sms_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sweatbox_sms_test.Interfaces.MyAPIService;
import com.example.sweatbox_sms_test.Interfaces.RefreshableFragment;
import com.example.sweatbox_sms_test.Models.UserModel;
import com.example.sweatbox_sms_test.Utils.SMSBroadcastReceiver;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int INTERVAL_FIFTEEN_MINUTES = 15 * 60 * 1000;
    private boolean isAutomaticSMSChecked = true;
    private static final String TAG = "MainActivity";
    private static final long DELAY_BETWEEN_MESSAGES = 15000;
//        private static final String API_URL = "http://10.0.2.2:8000/api/"; // for dev
    private static final String API_URL = "https://sweatbox-backend-production.up.railway.app/api/"; // for prod

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        nineAmAlarm(alarmManager);
        threePmAlarm(alarmManager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sweatbox");
        toolbar.inflateMenu(R.menu.menu_main);

        // select the first tab and render the respective fragment
        tabLayout.selectTab(tabLayout.getTabAt(0));
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new NewlyRegisteredFragment();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(MainActivity.this, "selected tab: " + tab.getText(), Toast.LENGTH_SHORT).show();

                int position = tab.getPosition();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment = null;

                switch (position) {
                    case 0:
                        fragment = new NewlyRegisteredFragment();
                        break;
                    case 1:
                        fragment = new RenewedFragment();
                        break;
                    case 2:
                        fragment = new NearExpirationFragment();
                        break;
                    case 3:
                        fragment = new ExpiredFragment();
                }

                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.start_cancel_alarm) {
            if (isAutomaticSMSChecked) {
                item.setTitle("Start automatic SMS");
                isAutomaticSMSChecked = !isAutomaticSMSChecked;
                cancelSMSBroadcast(this);
            } else {
                item.setTitle("Cancel automatic SMS");
                isAutomaticSMSChecked = !isAutomaticSMSChecked;
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                nineAmAlarm(alarmManager);
                threePmAlarm(alarmManager);
            }
            return true;
        } else if (itemId == R.id.send_now) {
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
            sendNow(smsManager, myAPIService);
            return true;
        } else if (itemId == R.id.refresh) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

            if (currentFragment != null && currentFragment instanceof RefreshableFragment) {
                RefreshableFragment refreshableFragment = (RefreshableFragment) currentFragment;
                refreshableFragment.fetchUser();
            }
            Log.d(TAG, "onOptionsItemSelected: " + currentFragment);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelSMSBroadcast(Context context) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Cancel the nine AM alarm
        Intent nineAmIntent = new Intent(this, SMSBroadcastReceiver.class);
        nineAmIntent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        PendingIntent nineAmPendingIntent = PendingIntent.getBroadcast(this, 0, nineAmIntent, PendingIntent.FLAG_MUTABLE);
        Log.d(TAG, "cancelSMSBroadcast: 9AM CANCELED");
        alarmManager.cancel(nineAmPendingIntent);
        nineAmPendingIntent.cancel();

        // Cancel the three PM alarm
        Intent threePmIntent = new Intent(this, SMSBroadcastReceiver.class);
        threePmIntent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        PendingIntent threePmPendingIntent = PendingIntent.getBroadcast(this, 1, threePmIntent, PendingIntent.FLAG_MUTABLE);
        Log.d(TAG, "cancelSMSBroadcast: 3PM CANCELED");
        alarmManager.cancel(threePmPendingIntent);
        threePmPendingIntent.cancel();
    }

    private void sendNow(SmsManager smsManager, MyAPIService myAPIService) {
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

    private void nineAmAlarm(AlarmManager alarmManager) {
        Intent intent = new Intent(this, SMSBroadcastReceiver.class);
        intent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        intent.putExtra("nineAmAlarm", true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the current time is already past 9 AM, schedule the alarm for the next day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long startTime = calendar.getTimeInMillis();
        Log.d(TAG, "nineAmAlarm: " + startTime + " " + calendar.getTime());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }

    private void threePmAlarm(AlarmManager alarmManager) {
        Intent intent = new Intent(this, SMSBroadcastReceiver.class);
        intent.setAction(SMSBroadcastReceiver.ACTION_SMS_RECEIVED);
        intent.putExtra("threePmAlarm", true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_MUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the current time is already past 3 PM, schedule the alarm for the next day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long startTime = calendar.getTimeInMillis();
        Log.d(TAG, "threePmAlarm: " + startTime + " " + calendar.getTime());
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }
}