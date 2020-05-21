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

package labelingStudy.nctu.minuku_2;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import io.fabric.sdk.android.Fabric;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.event.DecrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.event.IncrementLoadingProcessCountEvent;
import labelingStudy.nctu.minuku.logger.Log;
//import labelingStudy.nctu.minuku_2.controller.CounterActivity;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku_2.controller.DeviceIdPage;
import labelingStudy.nctu.minuku_2.service.BackgroundService;
import labelingStudy.nctu.minuku_2.view.customview.AdvertisementActivity;

import static labelingStudy.nctu.minuku_2.RecyclerViewAdapter.selectedPosition1;
import static labelingStudy.nctu.minuku_2.RecyclerViewAdapter.selectedPosition2;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String current_task;

    private AtomicInteger loadingProcessCount = new AtomicInteger(0);
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;

    public static String task="PART"; //default is PART
    ArrayList viewList;
    public final int REQUEST_ID_MULTIPLE_PERMISSIONS=1;
    public static View timerview,recordview,checkpointview;

    public static android.support.design.widget.TabLayout mTabs;
    public static ViewPager mViewPager;

    private SharedPreferences sharedPrefs;

    private boolean firstTimeOrNot;

    private AlertDialog enableNotificationListenerAlertDialog;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


    public static ImageView imageShow;
    Button bored;
    Button not_bored;
    private Button btn_private;
    private ArrayList<String> labelList;
    private ArrayList<String> startList;
    private ArrayList<String> endList;
    private ArrayList<String> indexList;

    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Boolean> mCheck = new ArrayList<>();
    private ArrayList<String> mStart = new ArrayList<>();
    private ArrayList<String> mEnd = new ArrayList<>();
    private ArrayList<String> mLabel = new ArrayList<>();
    private ArrayList<String> mIndex = new ArrayList<>();

    private ArrayList<Integer> deleteList = new ArrayList<>();

    public static int viewPosition = 0;


    File imgFile;
    static int totalsize = 0;
    public static int controlIndex = 0;

    public static boolean uploadConfirm = false;

    public static int resetIndex = -1;



    //Firebase
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    LinearLayoutManager layoutManager;// = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating Main activity");

        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        imageShow = (ImageView)findViewById(R.id.image_show);
        bored = (Button)findViewById(R.id.btn_bored);
        not_bored = (Button)findViewById(R.id.btn_notbored);
        btn_private = (Button)findViewById(R.id.btn_private);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        sharedPrefs = getSharedPreferences(Constants.sharedPrefString, MODE_PRIVATE);

//        current_task = getResources().getString(R.string.current_task);

        sharedPrefs.edit().putString("currentWork", Constants.currentWork).apply();

        String test = sharedPrefs.getString("Index", null);
        Log.d(TAG, "1 IndexNum:" + test);


//        EventBus.getDefault().register(this);
        mManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        if(!isNotificationServiceEnabled()) {
            android.util.Log.d(TAG, "notification start!!");
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }else{
            toggleNotificationListenerService();
        }

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        int sdk_int = Build.VERSION.SDK_INT;
        if(sdk_int>=23) {
            Log.d(TAG,"firstTimeOrNot @ OnCreate : "+ firstTimeOrNot);

            if(firstTimeOrNot) {
                requestCapturePermission();
                checkAndRequestPermissions();

            }
            getImages();
        }else{
            startServiceWork();

        }
//        startService(new Intent(getBaseContext(), BackgroundService.class));
        startService(new Intent(getBaseContext(), NotificationListenService.class));

        bored.setOnClickListener(doBoredClick);
        not_bored.setOnClickListener(doNotBoredClick);
        btn_private.setOnClickListener(doPrivateClick);


//COMMENT
//        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
//        Log.d(TAG,"firstTimeOrNot : "+ firstTimeOrNot);
//
//        if(firstTimeOrNot) {
//            startpermission();
//            firstTimeOrNot = false;
//            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
//        }


    }

    Button.OnClickListener doBoredClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {


//            Toast.makeText(MainActivity.this, "Bored Click", Toast.LENGTH_SHORT).show();
//                    mLabel.set(selectedPosition1, "bored");
//                    mLabel.set(selectedPosition2, "bored");
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("確定標記為『殺時間』?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(selectedPosition1 != -1 && selectedPosition2 != -1) {

                                LabelRetrieval();
                                StartRetrieval();
                                EndRetrieval();
                                IndexRetrieval();
//
                                viewPosition = selectedPosition2;
                                selectedPosition1 = -1;
                                selectedPosition2 = -1;

                                getImages();
//                                recyclerView.postDelayed(new Runnable(){
//                                    @Override
//                                    public void run() {
//                                        recyclerView.smoothScrollToPosition(viewPosition);
//                                    }
//                                }, 1000);
                            }else{
                                new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                                        .setMessage("Please choose an interval")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();

        }
    };

    Button.OnClickListener doNotBoredClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {


//            Toast.makeText(MainActivity.this, "Not Bored Click", Toast.LENGTH_SHORT).show();
//                    mLabel.set(selectedPosition1, "bored");
//                    mLabel.set(selectedPosition2, "bored");
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("確定標記為『非殺時間』?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(selectedPosition1 != -1 && selectedPosition2 != -1) {

                                LabelRetrieval2();
                                StartRetrieval();
                                EndRetrieval();
                                IndexRetrieval();
//
                                viewPosition = selectedPosition2;

                                selectedPosition1 = -1;
                                selectedPosition2 = -1;

                                getImages();
                            }else{
                                new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                                        .setMessage("Please choose an interval")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();

        }
    };

    Button.OnClickListener doPrivateClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {


//            Toast.makeText(MainActivity.this, "Private Click", Toast.LENGTH_SHORT).show();
//                    mLabel.set(selectedPosition1, "bored");
//                    mLabel.set(selectedPosition2, "bored");
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("確定標記為『無法分類』?")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(selectedPosition1 != -1 && selectedPosition2 != -1) {

                                LabelRetrieval3();
                                StartRetrieval();
                                EndRetrieval();
                                IndexRetrieval3();
//
                                viewPosition = selectedPosition2;

                                selectedPosition1 = -1;
                                selectedPosition2 = -1;

                                getImages();
                            }else{
                                new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                                        .setMessage("Please choose an interval")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();

        }
    };

    public void getImages(){
        android.util.Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        String test = sharedPrefs.getString("Index", null);
        Log.d(TAG, "2 IndexNum:" + test);

        imgFile = new File(Environment.getExternalStoragePublicDirectory(
                Constants.PACKAGE_DIRECTORY_PATH), "Demo");
        if(imgFile.exists()){
//            Toast.makeText(this, imgFile + "exists", Toast.LENGTH_LONG).show();
            android.util.Log.d(TAG,  "Path exists: " + imgFile);
            String t1 = sharedPrefs.getString("Index", null);
            Log.d(TAG, "3 IndexNum:" + t1);
            File[] files = imgFile.listFiles();
//            Log.d(TAG, "Files Size: "+ files.length);
            mImages.clear();
            mNames.clear();
            mCheck.clear();
            mLabel.clear();
            mStart.clear();
            mEnd.clear();
            mIndex.clear();
            Log.d(TAG, "131" + imgFile.listFiles());
            if(imgFile.listFiles()!=null){
                Log.d(TAG, "131進"  );

                for (int i = 0; i < files.length; i++)
                {
                    android.util.Log.d(TAG, "FileName:" + files[i].getName());
                    mImages.add(imgFile.toString() + "/" + files[i].getName());
//                    mNames.add(files[i].getName().substring(0, 8) + " " + files[i].getName().substring(8, 10) + ":" +files[i].getName().substring(10, 12) + ":" + files[i].getName().substring(12, 14));
                    mNames.add(files[i].getName());
                    mCheck.add(false);


                    //Get data and turn String to ArrayList
                    if(i<totalsize){
                        String serializedObject = sharedPrefs.getString("Label", null);
                        android.util.Log.d(TAG, "Label SerializeObject: " + serializedObject);
                        labelList = new ArrayList<>();
                        if (serializedObject != null){
                            Gson gson1 = new Gson();
                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
                            labelList = gson1.fromJson(serializedObject, type);
                            android.util.Log.d(TAG, "SerializeObject: " + labelList);
                            Log.d(TAG, "labellist length:" + labelList.size() + "index:" + i);
                            mLabel.add(labelList.get(i));
                        }
                    }
                    else
                        mLabel.add("NA");

                    if(i<totalsize){
                        //Get data and turn String to ArrayList
                        String serializedObject1 = sharedPrefs.getString("Start", null);
                        android.util.Log.d(TAG, "Start SerializeObject: " + serializedObject1);
                        startList = new ArrayList<>();
                        if (serializedObject1 != null){
                            Gson gson1 = new Gson();
                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
                            startList = gson1.fromJson(serializedObject1, type);
                            android.util.Log.d(TAG, "SerializeObject: " + startList);
                            mStart.add(startList.get(i));
                        }
                    }
                    else
                        mStart.add("0");


                    //Get data and turn String to ArrayList
                    if(i<totalsize){
                        String serializedObject2 = sharedPrefs.getString("End", null);
                        android.util.Log.d(TAG, "End SerializeObject: " + serializedObject2);
                        endList = new ArrayList<>();
                        if (serializedObject2 != null){
                            Gson gson1 = new Gson();
                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
                            endList = gson1.fromJson(serializedObject2, type);
                            android.util.Log.d(TAG, "SerializeObject: " + endList);
                            mEnd.add(endList.get(i));
                        }
                    }
                    else
                        mEnd.add("0");

                    if(i<totalsize){
                        String serializedObject2 = sharedPrefs.getString("Index", null);

                        android.util.Log.d(TAG, "Index SerializeObject: " + serializedObject2);
                        indexList = new ArrayList<>();
                        if (serializedObject2 != null){
                            Gson gson1 = new Gson();
                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
                            indexList = gson1.fromJson(serializedObject2, type);
                            android.util.Log.d(TAG, "SerializeObject: " + indexList);
                            mIndex.add(indexList.get(i));
                        }
                    }
                    else
                        mIndex.add("-1");



                }

                if(mImages.size() != 0){
                    Glide.with(this)
                            .asBitmap()
                            .load(mImages.get(0))
                            .into(imageShow);
                    Log.d(TAG, "mImages aa " +mImages);
                }

            }else{
//            Toast.makeText(this, imgFile + " not exists", Toast.LENGTH_LONG).show();
                android.util.Log.d(TAG,  "Path NOT exists: " + imgFile);


            }



        }

        if(resetIndex!=-1){
            for(int ri = 0; ri < mIndex.size(); ri ++){
                if(Integer.valueOf(mIndex.get(ri)) == resetIndex){
                    mLabel.set(ri, "NA");
                    mStart.set(ri, "0");
                    mEnd.set(ri, "0");
                    mIndex.set(ri, "-1");
                }
            }
            resetIndex = -1;
        }
        //Save data to preference
        Gson gson = new Gson();
        String json = gson.toJson(mLabel);
        sharedPrefs.edit().putString("Label", json).apply();
        totalsize = mLabel.size();
//
        //Save data to preference
        Gson gson1 = new Gson();
        String json1 = gson1.toJson(mStart);
        sharedPrefs.edit().putString("Start", json1).apply();
//
        //Save data to preference
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(mEnd);
        sharedPrefs.edit(). putString("End", json2).apply();

        //Save data to preference
        Gson gson3 = new Gson();
        String json3 = gson3.toJson(mIndex);
        sharedPrefs.edit(). putString("Index", json3).apply();

        initRecyclerView();
        recyclerView.postDelayed(new Runnable(){
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(viewPosition);
            }
        }, 1000);

    }

    public void LabelRetrieval() {
        String serializedObject = sharedPrefs.getString("Label", null);
        ArrayList<String> temp = new ArrayList<>();
        if (serializedObject != null) {
            Gson gson1 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp = gson1.fromJson(serializedObject, type);
            android.util.Log.d(TAG, "SerializeObject: " + temp);

            if (selectedPosition1 < selectedPosition2) {
                for (int i = selectedPosition1; i <= selectedPosition2; i++)
                    temp.set(i, "bored");
            } else {
                for (int i = selectedPosition2; i <= selectedPosition1; i++)
                    temp.set(i, "bored");
            }
            android.util.Log.d(TAG, "Label SerializeObject Result: " + temp);

            Gson gson = new Gson();
            String json = gson.toJson(temp);
            sharedPrefs.edit().putString("Label", json).apply();

        }
    }
    public void StartRetrieval() {

        String serializedObject1 = sharedPrefs.getString("Start", null);
        ArrayList<String> temp1 = new ArrayList<>();
        if (serializedObject1 != null) {
            Gson gson1 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp1 = gson1.fromJson(serializedObject1, type);
            if (selectedPosition1 < selectedPosition2) {
                temp1.set(selectedPosition1, "1");
            } else
                temp1.set(selectedPosition2, "1");

            Gson gson = new Gson();
            String json = gson.toJson(temp1);
            sharedPrefs.edit().putString("Start", json).apply();

            android.util.Log.d(TAG, "Start SerializeObject Result: " + temp1);

        }
    }
    public void EndRetrieval() {
        String serializedObject2 = sharedPrefs.getString("End", null);
        ArrayList<String> temp2 = new ArrayList<>();
        if (serializedObject2 != null) {
            Gson gson1 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp2 = gson1.fromJson(serializedObject2, type);
            if (selectedPosition1 < selectedPosition2) {
                temp2.set(selectedPosition2, "1");
            } else
                temp2.set(selectedPosition1, "1");

            Gson gson = new Gson();
            String json = gson.toJson(temp2);
            sharedPrefs.edit().putString("End", json).apply();

            android.util.Log.d(TAG, "End SerializeObject Result: " + temp2);

        }
    }
    public void IndexRetrieval(){
        String serializedObject3 = sharedPrefs.getString("Index", null);
        ArrayList<String> temp3 = new ArrayList<>();
        if (serializedObject3 != null) {
            Gson gson3 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp3 = gson3.fromJson(serializedObject3, type);
            controlIndex = controlIndex + 1;

            if (selectedPosition1 < selectedPosition2) {
                for (int i = selectedPosition1; i <= selectedPosition2; i++)
                    temp3.set(i, String.valueOf(controlIndex));
            } else {
                for (int i = selectedPosition2; i <= selectedPosition1; i++)
                    temp3.set(i, String.valueOf(controlIndex));
            }

            Gson gson = new Gson();
            String json = gson.toJson(temp3);
            sharedPrefs.edit().putString("Index", json).apply();

            android.util.Log.d(TAG, "Index SerializeObject Result: " + temp3);

            String t5 = sharedPrefs.getString("Index", null);
            Log.d(TAG, "7 IndexNum:" + t5);
        }
    }

    public void IndexRetrieval3(){
        String serializedObject4 = sharedPrefs.getString("Index", null);
        ArrayList<String> temp3 = new ArrayList<>();
        if (serializedObject4 != null) {
            Gson gson4 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp3 = gson4.fromJson(serializedObject4, type);
//            controlIndex = controlIndex + 1;

            if (selectedPosition1 < selectedPosition2) {
                for (int i = selectedPosition1; i <= selectedPosition2; i++)
                    temp3.set(i, String.valueOf(0));
            } else {
                for (int i = selectedPosition2; i <= selectedPosition1; i++)
                    temp3.set(i, String.valueOf(0));
            }

            Gson gson = new Gson();
            String json = gson.toJson(temp3);
            sharedPrefs.edit().putString("Index", json).apply();

            android.util.Log.d(TAG, "Index SerializeObject Result: " + temp3);

            String t5 = sharedPrefs.getString("Index", null);
            Log.d(TAG, "7 IndexNum:" + t5);
        }
    }

    public void LabelRetrieval2() {
        String serializedObject = sharedPrefs.getString("Label", null);
        ArrayList<String> temp = new ArrayList<>();
        if (serializedObject != null) {
            Gson gson1 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp = gson1.fromJson(serializedObject, type);
            android.util.Log.d(TAG, "SerializeObject: " + temp);

            if (selectedPosition1 < selectedPosition2) {
                for (int i = selectedPosition1; i <= selectedPosition2; i++)
                    temp.set(i, "not_bored");
            } else {
                for (int i = selectedPosition2; i <= selectedPosition1; i++)
                    temp.set(i, "not_bored");
            }
            android.util.Log.d(TAG, "Label SerializeObject Result: " + temp);

            Gson gson = new Gson();
            String json = gson.toJson(temp);
            sharedPrefs.edit().putString("Label", json).apply();

        }
    }

    public void LabelRetrieval3() {
        String serializedObject = sharedPrefs.getString("Label", null);
        ArrayList<String> temp = new ArrayList<>();
        if (serializedObject != null) {
            Gson gson1 = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            temp = gson1.fromJson(serializedObject, type);
            android.util.Log.d(TAG, "SerializeObject: " + temp);

            if (selectedPosition1 < selectedPosition2) {
                for (int i = selectedPosition1; i <= selectedPosition2; i++)
                    temp.set(i, Constants.BUTTON_PRIVATE);
            } else {
                for (int i = selectedPosition2; i <= selectedPosition1; i++)
                    temp.set(i, Constants.BUTTON_PRIVATE);
            }
            android.util.Log.d(TAG, "Label SerializeObject Result: " + temp);

            Gson gson = new Gson();
            String json = gson.toJson(temp);
            sharedPrefs.edit().putString("Label", json).apply();

        }
    }



    private void initRecyclerView(){
        android.util.Log.d(TAG, "initRecyclerView: init recyclerview");
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Log.d(TAG, "Select Position: " + selectedPosition1 + ", " +selectedPosition1+ ", " + viewPosition);

        Collections.sort(mImages);
        Collections.sort(mNames);
        Log.d(TAG, "mImages aa2"+ mImages);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(this, mNames, mImages, mCheck, mLabel, mStart, mEnd, mIndex);
//        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
//        recyclerView.getLayoutManager().scrollToPosition(viewPosition);
//        recyclerView.getLayoutManager().findViewByPosition(viewPosition);
    }



    private boolean isNotificationServiceEnabled(){
        android.util.Log.d(TAG, "isNotificationServiceEnabled");
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void toggleNotificationListenerService() {
        android.util.Log.d(TAG, "toggleNotificationListenerService");
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListenService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this, NotificationListenService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("start notification");
        alertDialogBuilder.setMessage("請開啟權限");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("no",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d(TAG,"onResume");

    }

    public void startpermission(){
        //Maybe useless in this project.
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));  // 協助工具

        Intent intent1 = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);  //usage
        startActivity(intent1);

        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//location
    }

    private void checkAndRequestPermissions() {

        Log.e(TAG,"checkingAndRequestingPermissions");

        int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();


        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
        }else{
            startServiceWork();
        }

    }


    public void getDeviceid(){

        TelephonyManager mngr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        int permissionStatus= ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if(permissionStatus==PackageManager.PERMISSION_GRANTED){
            Constants.DEVICE_ID = mngr.getDeviceId();
            if(Constants.DEVICE_ID == null){
                Constants.DEVICE_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            Log.e(TAG,"DEVICE_ID"+Constants.DEVICE_ID+" : "+mngr.getDeviceId());

        }
//        String uniqueID = UUID.randomUUID().toString();
//        Constants.DEVICE_ID = uniqueID;



    }

    public void startServiceWork(){

        getDeviceid();

        firstTimeOrNot = sharedPrefs.getBoolean("firstTimeOrNot", true);
        Log.d(TAG,"firstTimeOrNot @ startServiceWork : "+ firstTimeOrNot);

        if(firstTimeOrNot) {
            startpermission();
            firstTimeOrNot = false;
            sharedPrefs.edit().putBoolean("firstTimeOrNot", firstTimeOrNot).apply();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.BODY_SENSORS, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED){
                        android.util.Log.d("permission", "[permission test]all permission granted");
                        startServiceWork();
                    } else {
                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 0, "更新");
        menu.add(Menu.NONE, 2, 1, "上傳圖片");
//        menu.add(Menu.NONE, 3, 2, "手機ID");
        menu.add(Menu.NONE, 3, 2, "刪除圖片");
        menu.add(Menu.NONE, 4, 3, "手機ID");




        return super.onCreateOptionsMenu(menu);
    }

    public  int totallabel = 0;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            getImages();

        }else if (item.getItemId() == 2){
            new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                    .setMessage("確定資料標記完成，即將上傳？")
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //uploadImage();
                            Calendar current = Calendar.getInstance();
                            int currentHourIn24Format = current.get(Calendar.HOUR_OF_DAY);
                            int currentMinute = current.get(Calendar.MINUTE);

                            if ((currentHourIn24Format >= 10 && currentHourIn24Format < 22)){
                                Toast.makeText(MainActivity.this, "請於當日22時後再上傳圖片", Toast.LENGTH_SHORT).show();
                            }else if(!isNetworkAvailable()){

                                Toast.makeText(MainActivity.this, "請開啟任何網路連線", Toast.LENGTH_SHORT).show();
                            }else{
                                if(mIndex.contains("-1")){
                                    Toast.makeText(MainActivity.this, "有未標記圖片，請標記後再試一次", Toast.LENGTH_SHORT).show();

                                }else{
                                    for(int i =0; i < mIndex.size(); i++){
                                        if(Integer.valueOf(mIndex.get(i))>0){
                                            totallabel++;
                                        }
                                    }
                                    uploadImage();
//                                    Toast.makeText(MainActivity.this, "讚讚", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }else if(item.getItemId() == 4){
            Toast.makeText(this, Constants.DEVICE_ID, Toast.LENGTH_LONG).show();
        }else if(item.getItemId() == 3){
            final File[] files = imgFile.listFiles();
            for (int j = 0; j < files.length; j++) {
////
                File fdelete = new File(imgFile + "/" + files[j].getName());
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + imgFile + "/" + files[j].getName());

                    } else {
                        System.out.println("file not Deleted :" + imgFile + "/" + files[j].getName());
                    }
                }
            }

            mLabel.clear();
            mStart.clear();
            mEnd.clear();
            mIndex.clear();

            mImages.clear();
            mNames.clear();
            mCheck.clear();

            //Save data to preference
            Gson gson = new Gson();
            String json = gson.toJson(mLabel);
            sharedPrefs.edit().putString("Label", json).apply();
            totalsize = mLabel.size();
//
            //Save data to preference
            Gson gson1 = new Gson();
            String json1 = gson1.toJson(mStart);
            sharedPrefs.edit().putString("Start", json1).apply();
//
            //Save data to preference
            Gson gson2 = new Gson();
            String json2 = gson2.toJson(mEnd);
            sharedPrefs.edit(). putString("End", json2).apply();

            //Save data to preference
            Gson gson3 = new Gson();
            String json3 = gson3.toJson(mIndex);
            sharedPrefs.edit(). putString("Index", json3).apply();

            new AlertDialog.Builder(this)
                    .setMessage("完成刪除！")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getImages();
//                        Toast.makeText(MainActivity.this, "total: " + totallabel, Toast.LENGTH_SHORT).show();
                    }
            }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Firebase
    int count = 0;
    private void uploadImage() {
        final File[] files = imgFile.listFiles();
        int privateCount = 0;
        if (files != null) {

//            progressDialog = new ProgressDialog(this);
//            progressDialog.setProgress(files.length);
//            progressDialog.setMessage("Uploading...");
//            progressDialog.show();
            count = 0;
            for (int i = 0; i < files.length; i++) {
                StorageReference ref = storageReference.child(Constants.DEVICE_ID + "/" + files[i].getName());

                //--------- 判斷傳不傳 ---------
                String check_index = sharedPrefs.getString("Index", null);

//                android.util.Log.d(TAG, "(upload check) Get SerializeObject: " + check_index);
                ArrayList<String> checkList = new ArrayList<>();

                if (check_index != null){
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<String>>(){}.getType();
                    checkList = gson.fromJson(check_index, type);
                    android.util.Log.d(TAG, "(upload check) SerializeObject: " + checkList.get(i));

                    if(Integer.parseInt(checkList.get(i)) > 0){
                        ref.putFile(Uri.fromFile(new File(imgFile + "/" + files[i].getName())))
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                progressDialog.dismiss();
                                        count++;
                                        Toast.makeText(MainActivity.this, "圖片上傳中，請勿中斷網路連線: " + count+"/"+totallabel, Toast.LENGTH_SHORT).show();

                                        if(count == totallabel){
//                                            new AlertDialog.Builder(MainActivity.this)
//                                                    .setMessage("上傳完成")
//                                                    .show();
                                            uploadConfirm = true;
                                            totallabel = 0;
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
//                                progressDialog.dismiss();
                                        Toast.makeText(MainActivity.this, "upload Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
//                                                .getTotalByteCount());
//                                        count++;
//                                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
//                                        Toast.makeText(MainActivity.this, count + " 圖片上傳中:" + (int) progress + "%", Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(MainActivity.this, "圖片上傳中，請勿中斷網路連線: " + count+"/"+totallabel, Toast.LENGTH_SHORT).show();

                                    }
                                });
//                                .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Toast.makeText(MainActivity.this, count + "上傳完成", Toast.LENGTH_SHORT).show();
//
//                                    }
//                                });

                        uploadImageData(i);
                    }

                        if(Integer.parseInt(checkList.get(i)) == 0) {
                            privateCount++;


                        }

                }
                //--------------------------------


            }
            DatabaseReference myRef = database.getReference(Constants.DEVICE_ID);


            Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
            String str = formatter.format(curDate);
            String dd = str.replaceAll("\\D+", "").substring(0,8);



            myRef.child("NoCategoryCount").child(dd).setValue(privateCount);

//            for (int j = 0; j < deleteList.size(); j++) {
////
//                File fdelete = new File(imgFile + "/" + files[deleteList.get(j)].getName());
//                if (fdelete.exists()) {
//                    if (fdelete.delete()) {
//                        System.out.println("file Deleted :" + imgFile + "/" + files[deleteList.get(j)].getName());
//
//                        String serializedObject2 = sharedPrefs.getString("Index", null);
//
//                        android.util.Log.d(TAG, "Index SerializeObject: " + serializedObject2);
//                        indexList = new ArrayList<>();
//                        if (serializedObject2 != null){
//                            Gson gson1 = new Gson();
//                            Type type = new TypeToken<ArrayList<String>>(){}.getType();
//                            indexList = gson1.fromJson(serializedObject2, type);
//                            android.util.Log.d(TAG, "SerializeObject: " + indexList);
//                            mIndex.remove(deleteList.get(j));
//                        }
//
//                        //Save data to preference
//                        Gson gson3 = new Gson();
//                        String json3 = gson3.toJson(mIndex);
//                        sharedPrefs.edit(). putString("Index", json3).apply();
//
//
//                    } else {
//                        System.out.println("file not Deleted :" + imgFile + "/" + files[deleteList.get(j)].getName());
//                    }
//                }

//            deleteList = new ArrayList<>();

//            uploadImageData();


        }


    }
    private void uploadImageData(int index){
        DatabaseReference myRef = database.getReference(Constants.DEVICE_ID);
        Map<String, Object> imageRecord = new HashMap<>();
//        for(int c = 0; c < mImages.size(); c++){
            imageRecord.put("Image", mNames.get(index));
            imageRecord.put("Start", mStart.get(index));
            imageRecord.put("End", mEnd.get(index));
            imageRecord.put("Group", mIndex.get(index));
            imageRecord.put("Label", mLabel.get(index));

        Date curDate = new Date(System.currentTimeMillis()); // 獲取當前時間
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        String str = formatter.format(curDate);
        String dd = str.replaceAll("\\D+", "").substring(0,8);

            myRef.child("ImageRecord").child(dd).push().setValue(imageRecord);

//        }
    }

    //確認是否同意擷取螢幕內容
    public void requestCapturePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }


//android 5.0 up
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent it = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(it, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);

        switch (requestCode) {

            case 1:
                if (resultCode == RESULT_OK && it != null) {
                    BackgroundService.setResultIntent(it);
                    startService(new Intent(getBaseContext(), BackgroundService.class));
                    android.util.Log.e(TAG, "Start ScreenShotService");
                    Log.d(TAG,"Num: 7");

                }
                break;

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }




}
