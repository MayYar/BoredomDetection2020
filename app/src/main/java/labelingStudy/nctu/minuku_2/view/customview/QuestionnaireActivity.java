package labelingStudy.nctu.minuku_2.view.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.service.BackgroundService.q1;

public class QuestionnaireActivity extends AppCompatActivity {


    private static final String TAG = "QuestionnaireActivity";

    private final static String QuestionnaireTask1 = "請問您目前精神狀況如何？";
    private final static String QuestionnaireTask2 = "請問您現在心情如何？";
    private final static String QuestionnaireTask3 = "請問您現在忙碌程度如何？";

    TextView textView;
    CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5;
    Button submit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Log.d(TAG, "QuestionnaireActivity onCreate");

        textView = (TextView)findViewById(R.id.textView);
        checkBox1 = (CheckBox)findViewById(R.id.checkBox);
        checkBox2 = (CheckBox)findViewById(R.id.checkBox1);
        checkBox3 = (CheckBox)findViewById(R.id.checkBox2);
        checkBox4 = (CheckBox)findViewById(R.id.checkBox3);
        checkBox5 = (CheckBox)findViewById(R.id.checkBox4);
        submit = (Button)findViewById(R.id.button3);
        submit.setOnClickListener(doClick);

        textView.setText(getQNotificationcontent(q1));
        if(q1 == 1){
            checkBox1.setText("非常累");checkBox2.setText("稍累");checkBox3.setText("普通");checkBox4.setText("稍有精神");checkBox5.setText("非常有精神");
        }else if(q1 == 2){
            checkBox1.setText("非常不開心");checkBox2.setText("稍不開心");checkBox3.setText("普通");checkBox4.setText("稍開心");checkBox5.setText("非常開心");
        }else if(q1 == 3){
            checkBox1.setText("非常有空");checkBox2.setText("稍有空");checkBox3.setText("普通");checkBox4.setText("稍忙碌");checkBox5.setText("非常忙碌");
        }


        String ClickTime = ScheduleAndSampleManager.getTimeString(ScheduleAndSampleManager.getCurrentTimeInMillis());
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

            myRef_test.child("NotiRecord").child("QuestionnaireTask").push().setValue(noti_record);
        }

    }

    public static String getQNotificationcontent(int noti_id){
        String questionnaireText = "";
        if(noti_id == 1){
            questionnaireText = QuestionnaireTask1;
        }else if(noti_id == 2){
            questionnaireText = QuestionnaireTask2;
        }else if(noti_id == 3){
            questionnaireText = QuestionnaireTask3;
        }else{
            questionnaireText = "error";
        }

        return questionnaireText;
    }

    Button.OnClickListener doClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}
