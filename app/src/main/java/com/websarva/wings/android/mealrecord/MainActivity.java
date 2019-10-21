package com.websarva.wings.android.mealrecord;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //変数宣言
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    private SQLiteDatabase db;
    private int iYear;
    private int iMonth;
    private int iDate;
    private int iValue;
    private String strTime;
    private int score;
    private int test;
    private Calendar calendar;

    int bootCount = 0;
    int date=0;
    int currentdate;

    int lat;
    int lng;

    Location gps;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Button button;
    Intent intent;
    ContextAct mConAct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //ここから追記
        //レイアウトアイテム取得
        final SeekBar seekM = (SeekBar) findViewById(R.id.seekBar_Meal);
        final TextView Mscoretex = (TextView) findViewById(R.id.Meal_score);
        final ImageView faceScaleImageView = (ImageView) findViewById(R.id.Face_image);

        Button btnRecord =findViewById(R.id.btn_record);
        Button btnList =findViewById(R.id.btn_list);
        Button btGraph= findViewById(R.id.btn_graph);

        calendar = Calendar.getInstance();
        button = (Button) findViewById(R.id.btn_consult);
        button.setOnClickListener(btnTap);
        button.setEnabled(false);
        //デフォルトのSharedPreferencesから保存された"bootCount"を取得
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        bootCount = pref.getInt("bootCount", 1);
        date = pref.getInt("date", calendar.get(Calendar.MINUTE));
        Log.d("date ",String.valueOf(date) );

        //MainActivityのactivityとcontextを保持するクラスのインスタンス生成
        mConAct = new ContextAct(this, getApplicationContext());

        //画面のインスタンスを取得
        TextView textView= (TextView)findViewById(R.id.txt_count);
        //onCreateが呼ばれた回数を3桁で0埋めして表示用のTextViewに設定
        textView.setText(String.format("%1$03d", bootCount));

        //データベース取得
        helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        //シークバー
        Mscoretex.setText("痛み:"+seekM.getProgress());




        seekM.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //ツマミをドラッグしたときに呼ばれる

                        int Mscore;
                        Mscore = seekM.getProgress();

                        if(Mscore >= 0 && Mscore < 20){
                            faceScaleImageView.setImageResource(R.drawable.painf1);

                        }else if(Mscore >= 20 && Mscore < 40){
                            faceScaleImageView.setImageResource(R.drawable.painf2);

                        }else if(Mscore >= 40 && Mscore < 60){
                            faceScaleImageView.setImageResource(R.drawable.painf3);

                        }else if(Mscore >= 60 && Mscore < 80){
                            faceScaleImageView.setImageResource(R.drawable.painf4);

                        }else if(Mscore >= 80 && Mscore <= 100){
                            faceScaleImageView.setImageResource(R.drawable.painf5);

                        }


                        Mscoretex.setText("痛み:"+seekM.getProgress());
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                     // ツマミを離したときに呼ばれる

                    }

                }
        );

        //記録完了ボタン
        final AlertDialog.Builder RecordconfirmDialog=new AlertDialog.Builder(this);

        // ダイアログの設定
        RecordconfirmDialog.setTitle(R.string.dialog_title);      //タイトル設定
        RecordconfirmDialog.setMessage(R.string.dialog_msg);  //内容(メッセージ)設定

        // OK(肯定的な)ボタンの設定
        RecordconfirmDialog.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OKボタン押下時の処理
                // Log.d("AlertDialog", "Positive which :" + which);

                //OKボタン押下時の日付取得
                calendar = Calendar.getInstance();
                iYear = calendar.get(Calendar.YEAR);
                iMonth = calendar.get(Calendar.MONTH);
                iDate = calendar.get(Calendar.DATE);
                int iHour = calendar.get(Calendar.HOUR);
                int iMinute = calendar.get(Calendar.MINUTE);
                int iSecond = calendar.get(Calendar.SECOND);
                //string型に変換
                strTime = iHour +":"+iMinute +":"+iSecond;
                //categoryに満腹度を代入
                //複数のカテゴリがある場合、レイアウト等から該当するカテゴリを取ってきて代入
                //シークバーの数値を取ってくる
                score = seekM.getProgress();

                /**
                 * バックグラウンド処理開始
                 */
                MyJobService.schedule();

                gps = new LocationService().getLocation();



                lat = (int)(gps.getLatitude()*100);
                lng = (int)(gps.getLongitude()*100);

                //日付時刻と文字列をデータベースに記録
                insertData(db, iYear, iMonth, iDate, strTime, score, lng, lat);

                //完了画面表示
                Intent intent = new Intent(getApplication(), Recordfin_Activity.class);
                startActivity(intent);
            }
        });

        // NG(否定的な)ボタンの設定
        RecordconfirmDialog.setNegativeButton(R.string.dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NGボタン押下時の処理
                // Log.d("AlertDialog", "Negative which :" + which);
            }
        });


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecordconfirmDialog.show();


            }
        });


        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowList.class);
                startActivity(intent);
            }
        });
        btGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent = new Intent(getApplication(), ShowGraph.class);
                startActivity(intent);
            }
        });

    }

    //データベースに記録する関数
    //引数は データベース,日付文字列,カテゴリ,数値
    public void insertData(SQLiteDatabase db, int year, int month, int date, String time, int value, int lng, int lat) {

        ContentValues values = new ContentValues();
        values.put("year", year);
        values.put("month", month);
        values.put("date", date);
        values.put("time", time);
        values.put("value", value);
        values.put("longitude", lng);
        values.put("latitude", lat);

        db.insert("paindb", null, values);
    }
    @Override
    protected void onResume() {
        super.onResume();

        calendar = Calendar.getInstance();
        Resources res = getResources();
        int color = res.getColor(R.color.colorMain);


        /**
         * calendar.get(Calendar.MINUTE);
         * MINUTEにすると分ごとにカウントが増える
         * DATEなら1日ごと
         * SECONDなら秒ごと
         */
        currentdate = calendar.get(Calendar.MINUTE);
        Log.d("cdate ",String.valueOf(currentdate) );

        if(currentdate != date){
            date = currentdate;
            bootCount +=1;
            editor = pref.edit();
            editor.putInt("bootCount", bootCount);
            editor.commit();



            /**
             * if文の条件で右上のカウント数に応じて表示を変える
             */
            if(bootCount  >1) {
                button.setEnabled(true);
                new AlertDialog.Builder(this)
                        .setTitle("お知らせ")
                        .setMessage("先生と相談できるようになりました")
                        .setPositiveButton("OK", null)
                        .show();
                button.setBackgroundColor(color);


            }else {
                new AlertDialog.Builder(this)
                        //.setTitle("title")
                        .setMessage("今日も1日頑張っていきましょう！")
                        .setPositiveButton("OK", null)
                        .show();
                button.setBackgroundColor(Color.GRAY);
            }
        }
        //button.setEnabled(false);



        //画面のインスタンスを取得
        TextView textView= (TextView)findViewById(R.id.txt_count);
        //onCreateが呼ばれた回数を3桁で0埋めして表示用のTextViewに設定
        textView.setText(String.format("%1$03d", bootCount));
    }
    // ボタンAに OnClickListenerを実装する
    private View.OnClickListener btnTap = new View.OnClickListener() {

        // ボタンAクリック時に呼ばれるメソッド
        @Override
        public void onClick(View view) {

            String packageName = "com.google.android.gm";
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            startActivity(intent);
            bootCount=0;
            button.setBackgroundColor(Color.GRAY);
            button.setEnabled(false);


        }
    };

}
