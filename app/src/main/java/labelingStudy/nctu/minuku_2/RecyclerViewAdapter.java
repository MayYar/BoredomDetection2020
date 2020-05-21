package labelingStudy.nctu.minuku_2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import labelingStudy.nctu.minuku.config.Constants;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku_2.MainActivity.imageShow;
import static labelingStudy.nctu.minuku_2.MainActivity.resetIndex;
import static labelingStudy.nctu.minuku_2.MainActivity.viewPosition;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Boolean> mCheck = new ArrayList<>();
    private ArrayList<String> mStart = new ArrayList<>();
    private ArrayList<String> mEnd = new ArrayList<>();
    private ArrayList<String> mLabel = new ArrayList<>();
    private ArrayList<String> mIndex = new ArrayList<>();

    private SharedPreferences sharedPreferences;


    private Context mContext;
    public static int selectedPosition1 = -1;
    public static int selectedPosition2 = -1;
    private int checkCount = 0;

    public RecyclerViewAdapter(Context Context, ArrayList<String> mNames, ArrayList<String> mImages, ArrayList<Boolean> mCheck, ArrayList<String> mLabel, ArrayList<String> mStart, ArrayList<String> mEnd, ArrayList<String> mIndex) {
        this.mNames = mNames;
        this.mImages = mImages;
        this.mCheck = mCheck;
        this.mContext = Context;
        this.mLabel = mLabel;
        this.mStart = mStart;
        this.mEnd = mEnd;
        this.mIndex = mIndex;
        sharedPreferences = mContext.getSharedPreferences("com.example.jyl.mediaprojection", MODE_PRIVATE);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);

        ViewHolder holder = new ViewHolder(view);
        holder.image = (ImageView)view.findViewById(R.id.image);
        holder.name = (TextView)view.findViewById(R.id.name);
        holder.check = (ImageView)view.findViewById(R.id.check);
        holder.label = (View)view.findViewById(R.id.v_label);
        holder.start = (ImageView)view.findViewById(R.id.iv_start);
        holder.end = (ImageView)view.findViewById(R.id.iv_end);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder called");

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);

//        holder.name.setText(mNames.get(position));
//        Log.d(TAG, "File name: " + mNames.get(position));
//        holder.name.setText("");
//        if(mNames.get(position).substring(12,18).equals("00.jpg")){
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(mNames.get(position).substring(8,10) + ":" + mNames.get(position).substring(10,12));
//            holder.name.setText(mNames.get(position).substring(11,14));
//        Log.d(TAG, "File name: " + mNames.get(position).substring(8,14));

//        }else{
//            holder.name.setVisibility(View.INVISIBLE);
//        }

        if(mCheck.get(position))
            holder.check.setVisibility(View.VISIBLE);
        else
            holder.check.setVisibility(View.INVISIBLE);

        if(mLabel.get(position).equals("NA")){
            holder.label.setVisibility(View.INVISIBLE);
            holder.start.setVisibility(View.INVISIBLE);
            holder.end.setVisibility(View.INVISIBLE);

        }else if(mLabel.get(position).equals("bored")){
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setBackgroundColor(Color.parseColor("#a40404"));
            holder.start.setImageResource(R.drawable.bored_start_circle);
            holder.end.setImageResource(R.drawable.bored_start_circle);
            if(mStart.get(position).equals("1")){
                holder.start.setVisibility(View.VISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }
            else if(mEnd.get(position).equals("1")){
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.VISIBLE);
            }
            else{
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }

        }else if(mLabel.get(position).equals("not_bored")){
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setBackgroundColor(Color.parseColor("#0044BB"));
            holder.start.setImageResource(R.drawable.not_bored_start_circle);
            holder.end.setImageResource(R.drawable.not_bored_start_circle);
            if(mStart.get(position).equals("1")){
                holder.start.setVisibility(View.VISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }
            else if(mEnd.get(position).equals("1")){
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.VISIBLE);
            }
            else{
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }
        }else if(mLabel.get(position).equals(Constants.BUTTON_PRIVATE)){
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setBackgroundColor(Color.parseColor("#8E8E8E"));
            holder.start.setImageResource(R.drawable.private_start_circle);
            holder.end.setImageResource(R.drawable.private_start_circle);
            if(mStart.get(position).equals("1")){
                holder.start.setVisibility(View.VISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }
            else if(mEnd.get(position).equals("1")){
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.VISIBLE);
            }
            else{
                holder.start.setVisibility(View.INVISIBLE);
                holder.end.setVisibility(View.INVISIBLE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClick: clicked on an image: " + mImages.get(position));
                Toast.makeText(mContext, Integer.toString(position), Toast.LENGTH_SHORT).show();

                Glide.with(mContext)
                        .asBitmap()
                        .load(mImages.get(position))
                        .into(imageShow);

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //以標記
                if(Integer.parseInt(mIndex.get(position)) != -1){
                    new AlertDialog.Builder(mContext)
                            .setTitle("Warning")
                            .setMessage("已標記圖片，要清除標記嗎？(若選擇是，請再次點選右上角重新整理)")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    resetIndex = Integer.parseInt(mIndex.get(position));
                                    viewPosition = position;
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
//                            .setMessage("Labeled between " + mIndex.indexOf(mIndex.get(position)) + " and " + mIndex.lastIndexOf(mIndex.get(position)) + " \nAre you sure to reset?")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    String serializedObject = sharedPreferences.getString("Label", null);
//                                    ArrayList<String> temp = new ArrayList<>();
//                                    if (serializedObject != null) {
//                                        Gson gson1 = new Gson();
//                                        Type type = new TypeToken<ArrayList<String>>() {
//                                        }.getType();
//                                        temp = gson1.fromJson(serializedObject, type);
//                                        Log.d(TAG, "SerializeObject: " + temp);
//
//                                        for (int a = mIndex.indexOf(mIndex.get(position)); a <= mIndex.lastIndexOf(mIndex.get(position)); a++)
//                                            temp.set(a, "NA");
//
//                                        Log.d(TAG, "Label SerializeObject Result: " + temp);
//
//                                        Gson gson = new Gson();
//                                        String json = gson.toJson(temp);
//                                        sharedPreferences.edit().putString("Label", json).apply();
//
//                                    }
//                                    //-------------------------------------------------------
//                                    String serializedObject1 = sharedPreferences.getString("Start", null);
//                                    ArrayList<String> temp1 = new ArrayList<>();
//                                    if (serializedObject1 != null) {
//                                        Gson gson1 = new Gson();
//                                        Type type = new TypeToken<ArrayList<String>>() {
//                                        }.getType();
//                                        temp1 = gson1.fromJson(serializedObject1, type);
//
//                                        temp1.set(mIndex.indexOf(mIndex.get(position)), "0");
//
//                                        temp1.set(mIndex.lastIndexOf(mIndex.get(position)), "0");
//
//                                        Gson gson = new Gson();
//                                        String json = gson.toJson(temp1);
//                                        sharedPreferences.edit().putString("Start", json).apply();
//
//                                        Log.d(TAG, "Start SerializeObject Result: " + temp1);
//
//                                    }
//                                    //-------------------------------------------------------
//                                    String serializedObject2 = sharedPreferences.getString("End", null);
//                                    ArrayList<String> temp2 = new ArrayList<>();
//                                    if (serializedObject2 != null) {
//                                        Gson gson1 = new Gson();
//                                        Type type = new TypeToken<ArrayList<String>>() {
//                                        }.getType();
//                                        temp2 = gson1.fromJson(serializedObject2, type);
//
//                                        temp2.set(mIndex.indexOf(mIndex.get(position)), "0");
//
//                                        temp2.set(mIndex.lastIndexOf(mIndex.get(position)), "0");
//
//                                        Gson gson = new Gson();
//                                        String json = gson.toJson(temp2);
//                                        sharedPreferences.edit().putString("End", json).apply();
//
//                                        Log.d(TAG, "End SerializeObject Result: " + temp2);
//
//                                    }
//                                    //-------------------------------------------------------
//
//
//                                    String serializedObject3 = sharedPreferences.getString("Index", null);
//                                    ArrayList<String> rs = new ArrayList<>();
//                                    if (serializedObject3 != null) {
//                                        Gson gson3 = new Gson();
//                                        Type type = new TypeToken<ArrayList<String>>() {
//                                        }.getType();
//                                        rs = gson3.fromJson(serializedObject3, type);
//
//                                        for (int a = mIndex.indexOf(mIndex.get(position)); a <= mIndex.lastIndexOf(mIndex.get(position)); a++)
//                                            rs.set(a, String.valueOf(0));
//
//                                        Gson gson = new Gson();
//                                        String json = gson.toJson(rs);
//                                        sharedPreferences.edit().putString("Index", json).apply();
//
//                                        Log.d(TAG, "(Change)Index SerializeObject Result: " + rs);
//
//
////                                        mContext.startActivity(new Intent(mContext,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
//
//
//                                    }
//                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            })
                            .show();

                }


                //未選擇 > 選擇
                else if(!mCheck.get(position)){

                    if(checkCount == 2){
                        int temp = -1;
                    new AlertDialog.Builder(mContext)
                            .setTitle("Warning")
                            .setMessage("You have already marked the interval between position " + (temp = (selectedPosition1<selectedPosition2)?selectedPosition1:selectedPosition2) + " and " + (temp = (selectedPosition1>selectedPosition2)?selectedPosition1:selectedPosition2))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                    }else{
                            mCheck.set(position, true);
                            checkCount++;
                            if(selectedPosition1 == -1)
                                selectedPosition1 = position;
                            else
                                selectedPosition2 = position;
                        }
                    }
                //選擇 > 未選擇
                else {
                    mCheck.set(position, false);
                    checkCount--;

                    if(selectedPosition1 == position)
                        selectedPosition1 = -1;
                    else
                        selectedPosition2 = -1;
                }

            Log.d(TAG, "selectedPosition1 = " + selectedPosition1 + "selectedPosition2 = " + selectedPosition2);



                notifyDataSetChanged();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView name;
        ImageView check;
        ImageView start;
        ImageView end;
        View label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
