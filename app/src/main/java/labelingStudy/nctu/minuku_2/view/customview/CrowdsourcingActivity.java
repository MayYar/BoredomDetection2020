package labelingStudy.nctu.minuku_2.view.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.service.BackgroundService.c1;

public class CrowdsourcingActivity extends AppCompatActivity {

    TextView overview, question;
    RadioGroup rg;
    RadioButton rb1, rb2, rb3, rb4, rb5;
    Button submit;

    private static final String TAG = "CrowdsourcingActivity";

    public final static String CrowdSourcingTask1 = "請問您附近wifi強度如何？";
    public final static String CrowdSourcingTask2 = "請問您附近噪音強度如何？";
    public final static String CrowdSourcingTask3 = "請問您附近空氣品質如何？";
    public final static String CrowdSourcingTask4 = "請問您目前所處空間有垃圾桶嗎？";
    public final static String CrowdSourcingTask5 = "請問您目前所處空間有飲水機嗎？";
    public final static String CrowdSourcingTask6 = "請問您目前所處空間周圍有空的插座嗎？？";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowdsourcing);

        Log.d(TAG, "CrowdsourcingActivity onCreate");


        overview = (TextView)findViewById(R.id.tv_overview);
        question = (TextView)findViewById(R.id.tv_question);
        rg = (RadioGroup)findViewById(R.id.rg);
        rb1 = (RadioButton)findViewById(R.id.radioButton);
        rb2 = (RadioButton)findViewById(R.id.radioButton2);
        rb3 = (RadioButton)findViewById(R.id.radioButton3);
        rb4 = (RadioButton)findViewById(R.id.radioButton4);
        rb5 = (RadioButton)findViewById(R.id.radioButton5);
        submit = (Button)findViewById(R.id.btn_submit);
        submit.setOnClickListener(doClick);

        rg.setOnCheckedChangeListener(doCheck);

        overview.setText("");
        question.setText(getNotificationcontent(c1));

        if(c1 == 1){
            rb1.setText("非常弱(或無)"); rb2.setText("稍弱"); rb3.setText("適中"); rb4.setText("稍強"); rb5.setText("非常強");
        }else if(c1 == 2){
            rb1.setText("非常弱(或無)"); rb2.setText("稍弱"); rb3.setText("適中"); rb4.setText("稍強"); rb5.setText("非常強");
        }else if(c1 == 3){
            rb1.setText("非常不乾淨"); rb2.setText("稍不乾淨"); rb3.setText("適中"); rb4.setText("稍乾淨"); rb5.setText("非常乾淨");
        }else if(c1 == 4){
            rb1.setVisibility(View.INVISIBLE); rb2.setText("是"); rb3.setVisibility(View.INVISIBLE); rb4.setText("否"); rb5.setVisibility(View.INVISIBLE);
        }else if(c1 == 5) {
            rb1.setVisibility(View.INVISIBLE); rb2.setText("是"); rb3.setVisibility(View.INVISIBLE); rb4.setText("否"); rb5.setVisibility(View.INVISIBLE);
        }else if(c1 == 6) {
            rb1.setVisibility(View.INVISIBLE); rb2.setText("是"); rb3.setVisibility(View.INVISIBLE); rb4.setText("否"); rb5.setVisibility(View.INVISIBLE);
        }

        String ClickTime = ScheduleAndSampleManager.getTimeString(ScheduleAndSampleManager.getCurrentTimeInMillis());
        Log.d(TAG, ClickTime);



        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef_test = database.getReference(Constants.DEVICE_ID);

//      myRef_test.child("test_time").push().setValue(ScheduleAndSampleManager.getTimeString(sharedPrefs.getLong("lastNotificationTime", -1)));

        if(getIntent().hasExtra("GenerateTime")){
            Log.d(TAG, "has Generated");

            Map<String, Object> noti_record = new HashMap<>();
            noti_record.put("GenerateTime", ScheduleAndSampleManager.getTimeString(getIntent().getLongExtra("GenerateTime",0)));
            noti_record.put("ClickTime", ClickTime);

            if(ScheduleAndSampleManager.getCurrentTimeInMillis() - getIntent().getLongExtra("GenerateTime",0) > 900000)
                noti_record.put("isExpired", 1);
            else
                noti_record.put("isExpired", 0);

            myRef_test.child("NotiRecord").child("CrowdSourcingTask").push().setValue(noti_record);
        }

    }

    private RadioGroup.OnCheckedChangeListener doCheck = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

        }
    };

    public static String getNotificationcontent(int noti_id){
        String crowdSourcingText = "";
        if(noti_id == 1){
            crowdSourcingText = CrowdSourcingTask1;
        }else if(noti_id == 2){
            crowdSourcingText = CrowdSourcingTask2;
        }else if(noti_id == 3){
            crowdSourcingText = CrowdSourcingTask3;
        }else if(noti_id == 4){
            crowdSourcingText = CrowdSourcingTask4;
        }else if(noti_id == 5) {
            crowdSourcingText = CrowdSourcingTask5;
        }else if(noti_id == 6){
            crowdSourcingText = CrowdSourcingTask6;
        }else{
            crowdSourcingText = "error";
        }

            return crowdSourcingText;
    }

    Button.OnClickListener doClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}
