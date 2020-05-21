package labelingStudy.nctu.minuku_2.view.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.service.BackgroundService.a1;

public class AdvertisementActivity extends AppCompatActivity {


    private static final String TAG = "AdvertisementActivity";

    private final static String Advertisement1 = "萌神降臨 點數加價購";
    private final static String Advertisement2 = "在地潮食 NEW TRENDY SNACKS";
    private final static String Advertisement3 = "全家Ｘ鮮乳坊 小農舒食集";
    private final static String Advertisement4 = "德國百年菲仕樂精品廚具現實4周集點送";
    private final static String Advertisement5 = "振興優惠卷";
    private final static String Advertisement6 = "全家健康志向 超級大麥第二波";
    private final static String Advertisement7 = "涼麵優惠";
    private final static String Advertisement8 = "匠吐司 歡慶週年";
    private final static String Advertisement9 = "CITY TEA現萃茶 果然好檬";
    private final static String Advertisement10 = "甜點、咖啡輕鬆配";



    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);

        webview = (WebView) findViewById(R.id.webview);
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.getSettings().setSupportZoom(true);
//        webview.getSettings().setBuiltInZoomControls(true);
//        webview.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器
        android.util.Log.d(TAG, "4a1a1a1:"+ a1);


        loadURL();




        String ClickTime = ScheduleAndSampleManager.getTimeString(ScheduleAndSampleManager.getCurrentTimeInMillis());

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef_test = database.getReference(Constants.DEVICE_ID);

//      myRef_test.child("test_time").push().setValue(ScheduleAndSampleManager.getTimeString(sharedPrefs.getLong("lastNotificationTime", -1)));

        if(getIntent().hasExtra("GenerateTime")){
            Log.d(TAG, "has Generated");

            Map<String, Object> noti_record = new HashMap<>();
            noti_record.put("CreatedTime", ScheduleAndSampleManager.getTimeString(getIntent().getLongExtra("GenerateTime",0)));
            noti_record.put("ResponseTime", ClickTime);

            if(ScheduleAndSampleManager.getCurrentTimeInMillis() - getIntent().getLongExtra("GenerateTime",0) > 900000)
                noti_record.put("isExpired", 1);
            else
                noti_record.put("isExpired", 0);

            myRef_test.child("NotiRecord").child("Advertisement").push().setValue(noti_record);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, 0, "重新整理");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 1){
            loadURL();
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getANotificationcontent(int noti_id){
        android.util.Log.d(TAG, "3a1a1a1:"+ a1);

        String advertisementText = "";
        if(noti_id == 1){
            advertisementText = Advertisement1;
        }else if(noti_id == 2){
            advertisementText = Advertisement2;
        }else if(noti_id == 3){
            advertisementText = Advertisement3;
        }else if(noti_id == 4){
            advertisementText = Advertisement4;
        }else if(noti_id == 5) {
            advertisementText = Advertisement5;
        }else if(noti_id == 6){
            advertisementText = Advertisement6;
        }else if(noti_id == 7){
            advertisementText = Advertisement7;
        }else if(noti_id == 8){
            advertisementText = Advertisement8;
        }else if(noti_id == 9){
            advertisementText = Advertisement9;
        }else if(noti_id == 10){
            advertisementText = Advertisement10;
        }else{
            advertisementText = "error";
        }

        return advertisementText;
    }

    public void loadURL(){
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebViewClient(new WebViewClient());

        if(a1 == 1)
            webview.loadUrl("https://prod-event.azurewebsites.net/sanrio_god/");
        else if(a1 == 2)
            webview.loadUrl("https://www.7-11.com.tw/special/article_new.aspx?itme=Event_E010");
        else if(a1 == 3)
            webview.loadUrl("https://prod-event.azurewebsites.net/ranchcoffee/");
        else if(a1 == 4)
            webview.loadUrl("https://www.7-11.com.tw/event/20fissler/index.aspx");
        else if(a1 == 5)
            webview.loadUrl("https://www.7-11.com.tw/event/coupon/index.aspx");
        else if(a1 == 6)
            webview.loadUrl("https://event.family.com.tw/famihealth/");
        else if(a1 == 7)
            webview.loadUrl("https://www.7-11.com.tw/freshfoods/6_Noodles/index.aspx");
        else if(a1 == 8)
            webview.loadUrl("https://prod-event.azurewebsites.net/artisanbaker/");
        else if(a1 == 9)
            webview.loadUrl("https://www.citycafe.com.tw/event/17XianCuiCha/index.html");
        else if(a1 == 10)
            webview.loadUrl("https://event.family.com.tw/2018_letscafe/single/index.html");
    }
}
