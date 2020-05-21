package labelingStudy.nctu.minuku_2.view.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.service.BackgroundService.a1;
import static labelingStudy.nctu.minuku_2.service.BackgroundService.n1;

public class NewsActivity extends AppCompatActivity {

    private static final String TAG = "NewsActivity";

    WebView webview;

    public static ArrayList<String> URLList = new ArrayList<>();
    public static ArrayList<String> titleList = new ArrayList<>();
    public static ArrayList<String> contentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        webview = (WebView) findViewById(R.id.news_webview);
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器
//        webview.loadUrl("https://technews.tw/2019/11/06/youtube-to-launch-youtube-music-and-youtube-premium-in-taiwan/");
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

            myRef_test.child("NotiRecord").child("News").push().setValue(noti_record);
        }
    }


    public static String getNNotificationcontent(int noti_id){
        android.util.Log.d(TAG, "3a1a1a1:"+ a1);

        String NewsContent = "";
        if(noti_id == 1){
            NewsContent = contentList.get(0);
        }else if(noti_id == 2){
            NewsContent = contentList.get(1);
        }else if(noti_id == 3){
            NewsContent = contentList.get(2);
        }else if(noti_id == 4){
            NewsContent = contentList.get(3);
        }else if(noti_id == 5){
            NewsContent = contentList.get(4);
        }else{
            NewsContent = "NA";
        }

        return NewsContent;
    }

    public void loadURL(){
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebViewClient(new WebViewClient());

        if(n1 == 1)
            webview.loadUrl(URLList.get(0));
        else if(n1 == 2)
            webview.loadUrl(URLList.get(1));
        else if(n1 == 3)
            webview.loadUrl(URLList.get(2));
        else if(n1 == 4)
            webview.loadUrl(URLList.get(3));
        else if(n1 == 5)
            webview.loadUrl(URLList.get(4));
        else
            Toast.makeText(this, "No URL", Toast.LENGTH_SHORT).show();


    }
}
