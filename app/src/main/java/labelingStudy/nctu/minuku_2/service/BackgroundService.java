/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku_2.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.MobilityManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.AppUsageStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.BatteryStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.RingerStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TelephonyStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.MainActivity;
import labelingStudy.nctu.minuku_2.PostNews;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Receiver.RestarterBroadcastReceiver;
//import labelingStudy.nctu.minuku_2.Receiver.WifiReceiver;
import labelingStudy.nctu.minuku_2.Utils;
import labelingStudy.nctu.minuku_2.controller.Dispatch;
import labelingStudy.nctu.minuku_2.manager.InstanceManager;
import labelingStudy.nctu.minuku_2.view.customview.AdvertisementActivity;
import labelingStudy.nctu.minuku_2.view.customview.CrowdsourcingActivity;
import labelingStudy.nctu.minuku_2.view.customview.NewsActivity;
import labelingStudy.nctu.minuku_2.view.customview.QuestionnaireActivity;

import static labelingStudy.nctu.minuku_2.MainActivity.uploadConfirm;
import static labelingStudy.nctu.minuku_2.view.customview.NewsActivity.URLList;
import static labelingStudy.nctu.minuku_2.view.customview.NewsActivity.contentList;
import static labelingStudy.nctu.minuku_2.view.customview.NewsActivity.titleList;

public class BackgroundService extends Service {


    private static final String TAG = "BackgroundService";

    final static String CHECK_RUNNABLE_ACTION = "checkRunnable";
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

//    WifiReceiver mWifiReceiver;
    IntentFilter intentFilter;

    MinukuStreamManager streamManager;

    private ScheduledExecutorService mScheduledExecutorService;
    ScheduledFuture<?> mScheduledFuture, mScheduledScreenShot, mScheduledNotification, mScheduledNews;

    private int ongoingNotificationID = 42;
    private String ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;

    NotificationManager mNotificationManager;

    public static boolean isBackgroundServiceRunning = false;
    public static boolean isBackgroundRunnableRunning = false;

    private SharedPreferences sharedPrefs;

    private static Intent resultIntentfromM;
    public String fileName = "NA";
    File saveFile;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    private ImageReader imageReader;
    private Handler handler;

    private long userShowTime;
    private int notifyNotificationID = 11;
    private int notifyNotificationCode = 300;
    int hintFlag = 0;
    int hintCase = 1;


    int notiType;
    public static int c1, q1, a1 = 0, n1 = 0;

    //firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Image image;
    Context mContext;

    public BackgroundService() {
        super();

    }

    @Override
    public void onCreate() {

        super.onCreate();
        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;

        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.NOTIFICATION_UPDATE_THREAD_SIZE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.ACTION_CONNECTIVITY_CHANGE);
//        mWifiReceiver = new WifiReceiver();
        saveFile = getMainDirectoryName();


        initWindow();
        initHandler();
        createImageReader();
        initMediaProjection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundRunnableRunning ? "+isBackgroundRunnableRunning);

        String onStart = "BackGround, start service";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onStart);
        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onStart);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        createSurveyNotificationChannel();
        createNotificationChannel();

        //make the WifiReceiver start sending availSite to the server.
//        registerReceiver(mWifiReceiver, intentFilter);
        registerConnectivityNetworkMonitorForAPI21AndUp();

        IntentFilter checkRunnableFilter = new IntentFilter(CHECK_RUNNABLE_ACTION);
        registerReceiver(CheckRunnableReceiver, checkRunnableFilter);

        //building the ongoing notification to the foreground
        startForeground(ongoingNotificationID, getOngoingNotification(ongoingNotificationText));

        if (!isBackgroundServiceRunning) {

            Log.d(TAG, "Initialize the Manager");

            isBackgroundServiceRunning = true;

            CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to judge the condition is ? "+(!InstanceManager.isInitialized()));

            if (!InstanceManager.isInitialized()) {

                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to start the runnable.");

                InstanceManager.getInstance(this);
                SessionManager.getInstance(this);
                MobilityManager.getInstance(this);

                updateNotificationAndStreamManagerThread();
            }
        }

        // read test file
//        FileHelper fileHelper = FileHelper.getInstance(getApplicationContext());
//        FileHelper.readTestFile();

        return START_REDELIVER_INTENT; //START_STICKY_COMPATIBILITY;
    }

    private void updateNotificationAndStreamManagerThread(){

        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(
                updateStreamManagerRunnable,
                10,
                Constants.STREAM_UPDATE_FREQUENCY,
                TimeUnit.SECONDS);

        mScheduledScreenShot = mScheduledExecutorService.scheduleAtFixedRate(
                updateScreenShotRunnable,
                60, //TODO: should be longer:user need to register & login
//                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // 2 min
                10, // TODO: 測試時間會跟實際不同
                TimeUnit.SECONDS);

        mScheduledNotification = mScheduledExecutorService.scheduleAtFixedRate(
                updateNotificationStatusRunnable,
                30, //TODO: should be longer:user need to register & login
//                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // 2 min
                10, // TODO: 測試時間會跟實際不同
                TimeUnit.SECONDS);

        mScheduledNews = mScheduledExecutorService.scheduleAtFixedRate(
                updateNewsRunnable,
                10, //TODO: should be longer:user need to register & login
//                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // 2 min
                14400, // TODO: 測試時間會跟實際不同
                TimeUnit.SECONDS);
    }

    Runnable updateNotificationStatusRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "!!!!! Update Status !!!!!");
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            userShowTime = currentTime;
            Log.d(TAG, "updateNotificationStatusRunnable called ");



//            userShowTimeString = ScheduleAndSampleManager.getTimeString(userShowTime);
//            Log.d(TAG, "time string: " + userShowTimeString);
            sendNotification();
            Log.d(TAG, "updateNotificationStatusRunnable(sendNotification) called ");

        }
    };

    Runnable updateNewsRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "!!!!! Update Status !!!!!");
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            userShowTime = currentTime;
            Log.d(TAG, "updateNewsRunnable called ");


            for(int i = 1; i < 4; i++) {
                DatabaseReference myRef = database.getReference("Tech").child("t"+i);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PostNews value = dataSnapshot.getValue(PostNews.class);
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "value is: " + value.content);
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "value is: " + value.URL);
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "value is: " + value.title);

                        URLList.add(value.URL);
                        titleList.add(value.title);
                        contentList.add(value.content);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        labelingStudy.nctu.minuku.logger.Log.d(TAG, "Failed to read value." + databaseError.toException());
                    }


                });
            }


//            userShowTimeString = ScheduleAndSampleManager.getTimeString(userShowTime);
//            Log.d(TAG, "time string: " + userShowTimeString);
            Log.d(TAG, "updateNewsRunnable called ");

        }
    };

    Runnable updateScreenShotRunnable = new Runnable() {
        @Override
        public void run() {
            startScreenShot();
        }
    };

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {

            Calendar current = Calendar.getInstance();
            int currentHourIn24Format = current.get(Calendar.HOUR_OF_DAY);
            int currentMinute = current.get(Calendar.MINUTE);

            if ((currentHourIn24Format >= 10 && currentHourIn24Format < 22)) {

                try {

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundServiceRunning ? " + isBackgroundServiceRunning);
                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundRunnableRunning ? " + isBackgroundRunnableRunning);
                    Log.d(TAG, "updateStreamManager called");
                    streamManager.updateStreamGenerators();


                } catch (Exception e) {

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Background, service update, stream, Exception");
                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, Utils.getStackTrace(e));
                }
                DatabaseReference myRef = database.getReference(Constants.DEVICE_ID);

                Map<String, Object> accessibility = new HashMap<>();
                accessibility.put("pack", AccessibilityStreamGenerator.getPack());
                accessibility.put("text", AccessibilityStreamGenerator.getText());
                accessibility.put("type", AccessibilityStreamGenerator.getType());
                accessibility.put("extra", AccessibilityStreamGenerator.getExtra());
                accessibility.put("detectedtime", AccessibilityStreamGenerator.getDetectedTime());

                Map<String, Object> activityrecognition = new HashMap<>();
                activityrecognition.put("ProbableActivities", ActivityRecognitionStreamGenerator.getsProbableActivities());
                activityrecognition.put("MostProbableActivity", ActivityRecognitionStreamGenerator.getsMostProbableActivity());
                activityrecognition.put("LatestDetectionTime", ActivityRecognitionStreamGenerator.getsLatestDetectionTime());

                Map<String, Object> appusage = new HashMap<>();
                appusage.put("LastestForegroundActivity", AppUsageStreamGenerator.getmLastestForegroundActivity());
                appusage.put("LastestForegroundPackage", AppUsageStreamGenerator.getmLastestForegroundPackage());
                appusage.put("sScreen_Status", AppUsageStreamGenerator.getScreen_Status());

                Map<String, Object> battery = new HashMap<>();
                battery.put("mBatteryLevel", BatteryStreamGenerator.getmBatteryLevel());
                battery.put("mBatteryPercentage", BatteryStreamGenerator.getmBatteryPercentage());
                battery.put("mBatteryChargingState", BatteryStreamGenerator.getmBatteryChargingState());
                battery.put("isCharging", BatteryStreamGenerator.isIsCharging());

                Map<String, Object> connectivity = new HashMap<>();
                connectivity.put("isIsWifiConnected", ConnectivityStreamGenerator.mIsWifiConnected);
                connectivity.put("NetworkType", ConnectivityStreamGenerator.mNetworkType);
                connectivity.put("isNetworkAvailable", ConnectivityStreamGenerator.mIsNetworkAvailable);
                connectivity.put("isIsConnected", ConnectivityStreamGenerator.mIsConnected);
                connectivity.put("isIsWifiAvailable", ConnectivityStreamGenerator.mIsWifiAvailable);
                connectivity.put("isIsMobileAvailable", ConnectivityStreamGenerator.mIsMobileAvailable);
                connectivity.put("isIsMobileConnected", ConnectivityStreamGenerator.mIsMobileConnected);

                Map<String, Object> ringer = new HashMap<>();
                ringer.put("RingerMode", RingerStreamGenerator.getmRingerMode());
                ringer.put("AudioMode", RingerStreamGenerator.getmAudioMode());
                ringer.put("StreamVolumeMusic", RingerStreamGenerator.getmStreamVolumeMusic());
                ringer.put("StreamVolumeNotification", RingerStreamGenerator.getmStreamVolumeNotification());
                ringer.put("StreamVolumeRing", RingerStreamGenerator.getmStreamVolumeRing());
                ringer.put("StreamVolumeVoicecall", RingerStreamGenerator.getmStreamVolumeVoicecall());
                ringer.put("StreamVolumeSystem", RingerStreamGenerator.getmStreamVolumeSystem());

                Map<String, Object> telephony = new HashMap<>();
                telephony.put("mNetworkOperatorName", TelephonyStreamGenerator.getmNetworkOperatorName());
                telephony.put("mCallState", TelephonyStreamGenerator.getmCallState());
                telephony.put("mPhoneSignalType", TelephonyStreamGenerator.getmPhoneSignalType());
                telephony.put("mGsmSignalStrength", TelephonyStreamGenerator.getmGsmSignalStrength());
                telephony.put("mLTESignalStrength_dbm", TelephonyStreamGenerator.getmLTESignalStrength_dbm());
                telephony.put("mCdmaSignalStrengthLevel", TelephonyStreamGenerator.getmCdmaSignalStrengthLevel());

                Map<String, Object> transportationmode = new HashMap<>();
                transportationmode.put("ConfirmedActivityString", TransportationModeStreamGenerator.getConfirmedActivityString());
                transportationmode.put("SuspectTime", TransportationModeStreamGenerator.getSuspectTime());
                transportationmode.put("suspectedStartActivity", TransportationModeStreamGenerator.getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStartActivityType()));
                transportationmode.put("suspectedEndActivity", TransportationModeStreamGenerator.getActivityNameFromType(TransportationModeStreamGenerator.getSuspectedStopActivityType()));


                final Map<String, Object> record = new HashMap<>();
                record.put("TimeString", new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss").format(new Date(System.currentTimeMillis())).replaceAll("\\D+", ""));
                record.put("TimeMillis", System.currentTimeMillis());
                record.put("Accessibility", accessibility);
                record.put("ActivityRecognition", activityrecognition);
                record.put("AppUsage", appusage);
                record.put("Battery", battery);
                record.put("Connectivity", connectivity);
                record.put("Ringer", ringer);
                record.put("Telephony", telephony);
                record.put("TransportationMode", transportationmode);


                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                String str = formatter.format(curDate);
                String dd = str.replaceAll("\\D+", "").substring(0,8);

//            myRef.setValue("test");
//                myRef.child("MinukuData").child(dd).push().setValue(record);

                Log.d(TAG, "DB connected (updateStream)");
            }
        }
    };




    private Notification getNotification() {

        Random r1 = new Random();
        c1 = r1.nextInt(7-1) + 1;
        Random r2 = new Random();

        q1 = r2.nextInt(4-1) + 1;
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        String text = "";



//        Random r = new Random();
//        int notiType = r.nextInt(4 - 1) + 1;
//        Log.d(TAG, "notiType = " + notiType);

        Intent resultIntent = new Intent();
        Log.d(TAG, "2a1a1a1:"+ a1);


        if(notiType == 1){
            bigTextStyle.setBigContentTitle("CrowdSourcing Task"); //Constants.SURVEY_CHANNEL_NAME
            text = CrowdsourcingActivity.getNotificationcontent(c1);
            bigTextStyle.bigText(CrowdsourcingActivity.getNotificationcontent(c1));
            resultIntent.setComponent(new ComponentName(this, CrowdsourcingActivity.class));
//        bigTextStyle.bigText(text);
        }else if(notiType == 2){
            bigTextStyle.setBigContentTitle("Questionnaire Task"); //Constants.SURVEY_CHANNEL_NAME
            text = QuestionnaireActivity.getQNotificationcontent(q1);
            bigTextStyle.bigText(QuestionnaireActivity.getQNotificationcontent(q1));
            resultIntent.setComponent(new ComponentName(this, QuestionnaireActivity.class));
        }else if(notiType == 3){
            bigTextStyle.setBigContentTitle("Advertisement"); //Constants.SURVEY_CHANNEL_NAME
            text = AdvertisementActivity.getANotificationcontent(a1);
            bigTextStyle.bigText(AdvertisementActivity.getANotificationcontent(a1));
            resultIntent.setComponent(new ComponentName(this, AdvertisementActivity.class));
        }else if(notiType == 4){
            bigTextStyle.setBigContentTitle("News"); //Constants.SURVEY_CHANNEL_NAME
            text = NewsActivity.getNNotificationcontent(n1);
            bigTextStyle.bigText(NewsActivity.getNNotificationcontent(n1));
            resultIntent.setComponent(new ComponentName(this, NewsActivity.class));
        }

//        resultIntent.setComponent(new ComponentName(this, CrowdsourcingActivity.class));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Bundle bundle = new Bundle();
//        bundle.putString(" GenerateTime", ScheduleAndSampleManager.getTimeString(userShowTime));
        bundle.putLong("GenerateTime", userShowTime);
        resultIntent.putExtras(bundle);

        PendingIntent pending = PendingIntent.getActivity(this, notifyNotificationCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle(Constants.APP_FULL_NAME)
//                .setContentText(text)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .setChannelId(Constants.SURVEY_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .build();
        }

    }

    private Notification getHintNotification() {


        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        String text = "";
        if(hintCase == 1)
            text = "請記得標記所有圖片並完成上傳和刪除的動作哦！";
        else if(hintCase == 2)
            text = "上傳即將完成！請務必記得執行刪除動作";

        bigTextStyle.setBigContentTitle("重要訊息!"); //Constants.SURVEY_CHANNEL_NAME
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent();
        resultIntent.setComponent(new ComponentName(this, MainActivity.class));

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pending = PendingIntent.getActivity(this, notifyNotificationCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle(Constants.APP_FULL_NAME)
//                .setContentText(text)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .setChannelId(Constants.SURVEY_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .build();
        }

    }

    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_FULL_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .setChannelId(Constants.ONGOING_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .build();
        }
    }

    private void sendNotification() {
        boolean send = false;
        boolean hint = false;

        long lastNotiTime = sharedPrefs.getLong("lastNotificationTime", -1);
        Log.d(TAG, "lastNotiTime:"+ lastNotiTime);

        Log.d(TAG, "lastNotiTime: " + ScheduleAndSampleManager.getTimeString(lastNotiTime));
        Log.d(TAG, "userShowTime: " + ScheduleAndSampleManager.getTimeString(userShowTime));


        Random r = new Random();
        notiType = r.nextInt(5 - 1) + 1;
//        Log.d(TAG, "notiType = " + notiType);

        int controlRandom = r.nextInt( 121 - 1) + 1;
        Log.d(TAG, "controlRandom = " + controlRandom);

        Calendar current = Calendar.getInstance();
        int currentHourIn24Format = current.get(Calendar.HOUR_OF_DAY);
        int currentMinute = current.get(Calendar.MINUTE);

        if ((currentHourIn24Format >= 10 && currentHourIn24Format < 22)) {
//                || (currentHourIn24Format == 22 && currentMinute <= 30)) {
            if ((userShowTime - lastNotiTime) > 3600000) { // 1 hr
                send = true;

                a1++;
                n1++;

                if(a1 == 11){
                    a1 = 1;
                }

                if(n1 == 6){
                    n1 = 1;
                }
            }
            hintFlag = 0;
        }
        Log.d(TAG, "hintFlag1" + hintFlag);
        if(currentHourIn24Format >= 22 && hintFlag == 0){
            hint = true;
            hintFlag = 1;
        }

        if(uploadConfirm){
            hintCase = 2;
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = getHintNotification();
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            mNotificationManager.notify(notifyNotificationID, notification);
            uploadConfirm = false;
        }

        if(hint){
            hintCase = 1;
            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = getHintNotification();
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            mNotificationManager.notify(notifyNotificationID, notification);        }
        Log.d(TAG, "hintFlag2" + hintFlag);

//        send = true; //////////TODO: for test //////////
//        notiType = 4;
//        controlRandom = 9;
        Log.d(TAG, "1a1a1a1:"+ a1);

        if (send) {
            if(controlRandom < 10){
                Log.d(TAG, "to send notification");
                sharedPrefs.edit()
                        .putLong("lastNotificationTime", userShowTime)
                        .apply();

                Map<String, Object> AllnotiRecord = new HashMap<>();

                NotificationManager mNotificationManager =
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//            String notificationText = "請點選通知查看您的狀態及填寫問卷!!";
//                String notificationText = "提供給您一則即時的科技新聞";

                Notification notification = getNotification();
                notification.defaults |= Notification.DEFAULT_VIBRATE;

                mNotificationManager.notify(notifyNotificationID, notification);

                if(notiType == 1){
                    Log.d(TAG, "notiType = CrowdSourcing Task");
                    AllnotiRecord.put("Type", "CrowdSourcing Task");
                    AllnotiRecord.put("Content", CrowdsourcingActivity.getNotificationcontent(c1));
                }else if(notiType == 2){
                    Log.d(TAG, "notiType = Questionnaire Task");
                    AllnotiRecord.put("Type", "Questionnaire Task");
                    AllnotiRecord.put("Content", QuestionnaireActivity.getQNotificationcontent(q1));
                }else if(notiType == 3){
                    Log.d(TAG, "notiType = Advertisement");
                    AllnotiRecord.put("Type", "Advertisement");
                    AllnotiRecord.put("Content", AdvertisementActivity.getANotificationcontent(a1));
                }else if(notiType == 4){
                    Log.d(TAG, "notiType = News");
                    AllnotiRecord.put("Type", "News");
                    AllnotiRecord.put("Content", NewsActivity.getNNotificationcontent(n1));
                }

                DatabaseReference myRef_test = database.getReference(Constants.DEVICE_ID);

                AllnotiRecord.put("CreatedTime", ScheduleAndSampleManager.getTimeString(sharedPrefs.getLong("lastNotificationTime", -1)));

                Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
                String str = formatter.format(curDate);
                String dd = str.replaceAll("\\D+", "").substring(0,8);

                myRef_test.child("NotiRecord").child("AllNoti").child(dd).push().setValue(AllnotiRecord);


            }else{
                //故意不發送notification
            }



        }
    }

    private int getNotificationIcon(Notification.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            notificationBuilder.setColor(Color.TRANSPARENT);
            return R.drawable.muilab_icon_noti;

        }
        return R.drawable.muilab_icon;
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();

        stopTheSessionByServiceClose();

        String onDestroy = "BackGround, onDestroy";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onDestroy);
        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onDestroy);

        sendBroadcastToStartService();

//        checkingRemovedFromForeground();
//        removeRunnable();

        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;


        mNotificationManager.cancel(ongoingNotificationID);

        Log.d(TAG, "Destroying service. Your state might be lost!");

        sharedPrefs.edit().putInt("CurrentState", TransportationModeStreamGenerator.mCurrentState).apply();
        sharedPrefs.edit().putInt("ConfirmedActivityType", TransportationModeStreamGenerator.mConfirmedActivityType).apply();

//        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(CheckRunnableReceiver);
    }

    @Override
    public void onTaskRemoved(Intent intent){
        super.onTaskRemoved(intent);

        sendBroadcastToStartService();

//        checkingRemovedFromForeground();
//        removeRunnable();

        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;


        mNotificationManager.cancel(ongoingNotificationID);

        String onTaskRemoved = "BackGround, onTaskRemoved";
        CSVHelper.storeToCSV(CSVHelper.CSV_CheckService_alive, onTaskRemoved);

        sharedPrefs.edit().putInt("CurrentState", TransportationModeStreamGenerator.mCurrentState).apply();
        sharedPrefs.edit().putInt("ConfirmedActivityType", TransportationModeStreamGenerator.mConfirmedActivityType).apply();
    }

    private void registerConnectivityNetworkMonitorForAPI21AndUp() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onAvailable(Network network) {
                        /*sendBroadcast(
                                getConnectivityIntent("onAvailable")
                        );*/
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities){
                        sendBroadcast(
                                getConnectivityIntent("onCapabilitiesChanged : "+networkCapabilities.toString())
                        );
                    }

                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLinkPropertiesChanged : "+linkProperties.toString())
                        );*/
                    }

                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLosing")
                        );*/
                    }

                    @Override
                    public void onLost(Network network) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLost")
                        );*/
                    }
                }
        );

    }

    private void checkingRemovedFromForeground(){

        Log.d(TAG,"stopForeground");

        stopForeground(true);

        try {

            unregisterReceiver(CheckRunnableReceiver);
        }catch (IllegalArgumentException e){

        }

        mScheduledExecutorService.shutdown();
    }

    private void stopTheSessionByServiceClose(){

        //if the background service is killed, set the end time of the ongoing trip (if any) using the current timestamp
        if (SessionManager.getOngoingSessionIdList().size()>0){

            Session session = SessionManager.getSession(SessionManager.getOngoingSessionIdList().get(0)) ;

            //if we end the current session, we should update its time and set a long enough flag
            if (session.getEndTime()==0){
                long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
                session.setEndTime(endTime);
            }

            //end the current session
            SessionManager.endCurSession(session);

            sharedPrefs.edit().putInt("ongoingSessionid",session.getId()).apply();
        }
    }

    private void removeRunnable(){

        mScheduledFuture.cancel(true);
        mScheduledScreenShot.cancel(true);
        mScheduledNotification.cancel(true);
    }

    private void sendBroadcastToStartService(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            sendBroadcast(new Intent(this, RestarterBroadcastReceiver.class).setAction(Constants.CHECK_SERVICE_ACTION));
        } else {

            Intent checkServiceIntent = new Intent(Constants.CHECK_SERVICE_ACTION);
            sendBroadcast(checkServiceIntent);
        }
    }

    private Intent getConnectivityIntent(String message) {

        Intent intent = new Intent();

        intent.setAction(Constants.ACTION_CONNECTIVITY_CHANGE);

        intent.putExtra("message", message);

        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.ONGOING_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.ONGOING_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createSurveyNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.SURVEY_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.SURVEY_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    BroadcastReceiver CheckRunnableReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(CHECK_RUNNABLE_ACTION)) {

                Log.d(TAG, "[check runnable] going to check if the runnable is running");

                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "going to check if the runnable is running");
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "is the runnable running ? " + isBackgroundRunnableRunning);

                if (!isBackgroundRunnableRunning) {

                    Log.d(TAG, "[check runnable] the runnable is not running, going to restart it.");

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is not running, going to restart it");

                    updateNotificationAndStreamManagerThread();

                    Log.d(TAG, "[check runnable] the runnable is restarted.");

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is restarted");
                }

                PendingIntent pi = PendingIntent.getBroadcast(BackgroundService.this, 0, new Intent(CHECK_RUNNABLE_ACTION), 0);

                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.set(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                        pi
                );
            }
        }
    };


    private WindowManager windowManager =null;
    private ImageView igv = null;
    private WindowManager.LayoutParams params;

    private int screenWidth,screenHeight,screenDensity;

    public void initWindow(){

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //取得螢幕的各項參數
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;


        Log.d(TAG,"density:"+screenDensity+", width:" + screenWidth + ", height:" + screenHeight);

        igv = new ImageView(this);
        igv.setImageResource(R.mipmap.ic_launcher);

    }

    public void initHandler(){
        handler = new Handler();
    }

    //建立imageReader
    public void createImageReader() {
//        if(imageReader == null)
        try {
            imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void initMediaProjection(){

        //透過MediaProjectionManager取得MediaProjection
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(AppCompatActivity.RESULT_OK, resultIntentfromM);

        //呼叫mediaProjection.createVirtualDisplay()
        try {
            virtualDisplay = mediaProjection.createVirtualDisplay("screen-mirror",
                    screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(), null, null);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "virtualDisplay is null", Toast.LENGTH_SHORT).show();
        }

    }

    public static void setResultIntent(Intent it){
        resultIntentfromM = it;
    }

    public void startScreenShot(){

        igv.setVisibility(View.GONE);

        Log.e(TAG, "startScreenShot called");
//        handler.postDelayed(new Runnable() {
//            public void run() {

        boolean start = false;



        Calendar current = Calendar.getInstance();
        int currentHourIn24Format = current.get(Calendar.HOUR_OF_DAY);
        int currentMinute = current.get(Calendar.MINUTE);

        if ((currentHourIn24Format >= 10 && currentHourIn24Format < 22)){
//            if (lastNotiTime != 0 && ((userShowTime - lastNotiTime) > 3600000)) { // 1 hr
                start = true;
//            }
        }
        if(start){
            if(!AppUsageStreamGenerator.getScreen_Status().equals("Screen_off"))
                startCapture();
//            }
//        },10000);


//        runnable = new Runnable() {
//            @Override
//            public void run() {
////                if(!AppUsageStreamGenerator.getScreenStatusinService().equals("Screen_off"))
//                startCapture();
//                //延时1秒post
//                handler.postDelayed(this, 10000);
//            }
//        };
        }

    }


    private void startCapture() {

        //呼叫image.acquireLatestImage()，取得image
//        Image image = null;
            image = imageReader.acquireLatestImage();

        new SaveTask().execute(image);
//        image.close();

    }


    public class SaveTask extends AsyncTask<Image, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return false;
            }

            Boolean success = false;


            Image image = params[0];
            //處理影像並儲存到手機
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * screenWidth;
            Bitmap bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
            image.close();
            File fileImage;
            if (bitmap != null) {
                try {
                    fileImage = getScreenShotsFile();
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth/2, screenHeight/2, true);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
                        out.flush();
                        out.close();
                        success = true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    bitmap.recycle();
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            if (bool) {
//                Toast.makeText(getApplicationContext(),"Got it",Toast.LENGTH_SHORT).show();
            }
            else{
                startCapture();
            }
            igv.setVisibility(View.VISIBLE);

        }
    }

    public File getMainDirectoryName() {
        //Here we will use getExternalFilesDir and inside that we will make our Demo folder
        //benefit of getExternalFilesDir is that whenever the app uninstalls the images will get deleted automatically.
        File mainDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Constants.PACKAGE_DIRECTORY_PATH), "Demo");

//        File mainDir = new File(Constants.PACKAGE_DIRECTORY_PATH, "Demo");

        Log.d(TAG, "Demo File is presented at " + mainDir );
//        mainDir.mkdirs();
//        //If File is not present create directory
        if (!mainDir.exists()) {
            mainDir.mkdirs();
            Log.d(TAG, "Directory not created");
        }
        return mainDir;
    }

    public File getScreenShotsFile(){
        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        String str = formatter.format(curDate);
        fileName = str.replaceAll("\\D+", "");
        File file = new File(saveFile.getAbsolutePath(), fileName+".jpg");
        return file;
//        String screenShot = FileUtils.getDiskCacheDir(getApplicationContext()) + File.separator + "ScreenShot";
//        if(!FileUtils.isFileExists(screenShot)){
//            FileUtils.creatSDDir(screenShot);
//        }
//        String tmp = screenShot + File.separator + System.currentTimeMillis() + ".jpg";
//
//        if(!FileUtils.isFileExists(tmp)){
//            FileUtils.createSDFile(tmp);
//        }
//        Log.i("path",tmp);
//
//        return new File(tmp);

    }

}
