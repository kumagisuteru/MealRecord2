package com.websarva.wings.android.mealrecord;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //変数宣言
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    private SQLiteDatabase db;
    private int iYear, iMonth, iDate, score, currentdate, lng, lat;
    private String strTime;
    private Calendar calendar;
    private int bootCount = 0;
    private int date=0;
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
        TextView textView= (TextView)findViewById(R.id.txt_count);

        calendar = Calendar.getInstance();
        //医師に相談ボタンは起動時押下不可
        button = (Button) findViewById(R.id.btn_consult);
        button.setOnClickListener(btnTap);
        button.setEnabled(false);

        //医師に相談ボタンを押下可能にするためのログインボーナス
        //SharedPreferences...データを簡易的に保存することが可能
        //判別するためのkeyと初期値を設定する...bootcountは日をまたいでアプリを開いた回数
        //dateは開いた日付
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        bootCount = pref.getInt("bootCount", 1);
        date = pref.getInt("date", calendar.get(Calendar.DATE));
        //アプリが開かれた回数を3桁で0埋めして表示用のTextViewに設定(アプリ右上)
        textView.setText(String.format("%1$03d", bootCount));

        //MainActivityのactivityとcontextを保持するクラスのインスタンス生成
        mConAct = new ContextAct(this, getApplicationContext());

        //データベース取得
        helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        //シークバー
        Mscoretex.setText("痛み:"+seekM.getProgress());

        //シークバーのリスナー
        seekM.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //ツマミをドラッグしたときに呼ばれる
                        int Mscore;
                        Mscore = seekM.getProgress();

                        //シークバーのスコアに応じて画像を変える
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

                        //シークバーの数値をテキストに表示する
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

        //記録完了ボタンを押したときのダイアログ表示のインスタンス生成
        final AlertDialog.Builder RecordconfirmDialog = new AlertDialog.Builder(this);

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
                iMonth = calendar.get(Calendar.MONTH)+1;
                iDate = calendar.get(Calendar.DATE);
                int iHour = calendar.get(Calendar.HOUR);
                int iMinute = calendar.get(Calendar.MINUTE);
                int iSecond = calendar.get(Calendar.SECOND);
                //時間をstring型に変換
                strTime = iHour +":"+iMinute +":"+iSecond;
                //シークバーのスコア取得
                score = seekM.getProgress();

                /**
                 * バックグラウンド処理開始
                 */
                MyJobService.schedule();

                //記録時の位置情報取得
                gps = new LocationService().getLocation();
                lat = (int)(gps.getLatitude()*100);
                lng = (int)(gps.getLongitude()*100);

                //データベースに記録
                insertData(db, iYear, iMonth, iDate, strTime, score, lng, lat);
               /* insertData(db, 2019, 11, 1, "10:10:10", 80, 0, 0);
                insertData(db, 2019, 11, 2, "10:10:10", 60, 0, 0);
                insertData(db, 2019, 11, 3, "10:10:10", 65, 0, 0);
                insertData(db, 2019, 11, 4, "10:10:10", 55, 0, 0);
                insertData(db, 2019, 11, 5, "10:10:10", 45, 0, 0);
                insertData(db, 2019, 11, 6, "10:10:10", 60, 0, 0);
                insertData(db, 2019, 11, 7,"10:10:10", 20, 0, 0);
                insertData(db, 2019, 11, 8,"10:10:10", 50, 0, 0);
                insertData(db, 2019, 11, 9, "10:10:10", 80, 0, 0);
                insertData(db, 2019, 11, 10, "10:10:10", 30, 0, 0);
                insertData(db, 2019, 11, 11, "10:10:10", 70, 0, 0);
                insertData(db, 2019, 11, 12, "10:10:10", 55, 0, 0);
                insertData(db, 2019, 11, 13, "10:10:10", 40, 0, 0);
*/
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

        //記録ボタンのリスナー
        //ダイアログを表示する
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordconfirmDialog.show();

            }
        });

        //リストボタン押下時の処理
        //リスト画面に遷移
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplication(), ShowList.class);
                startActivity(intent);
            }
        });

        //グラフボタン押下時の処理
        //グラフ画面に遷移
        btGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplication(), ShowGraph.class);
                startActivity(intent);
            }
        });
    }


    //各種データをデータベースに記録する関数...引数は記録するデータ
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

    //アプリ再開時の処理
    @Override
    protected void onResume() {
        super.onResume();

        //インスタンス生成
        calendar = Calendar.getInstance();
        Resources res = getResources();
        int color = res.getColor(R.color.colorMain);

        /**
         * calendar.get(Calendar.MINUTE);
         * MINUTEにすると分ごとにカウントが増える
         * DATEなら1日ごと
         * SECONDなら秒ごと
         */
        //現在の日にちを取得する
        currentdate = calendar.get(Calendar.DATE);

        //現在の日にちとSharedpreferencesに保存していたdateを比較
        //違ったら(=日が進んでいたら)，bootcountを増やして
        //dateとbootcountを更新する
        if(currentdate != date){
            date = currentdate;
            bootCount +=1;
            editor = pref.edit();
            editor.putInt("bootCount", bootCount);
            editor.commit();


            /**
             * if文の条件で右上のカウント数に応じて表示を変える
             */
            //bootcountが一定以上ならば，医師に相談ボタンを押下可能(かつ色変更)にして，ダイアログ表示
            if(bootCount  > 3) {
                button.setEnabled(true);
                new AlertDialog.Builder(this)
                        .setTitle("お知らせ")
                        .setMessage("先生と相談できるようになりました")
                        .setPositiveButton("OK", null)
                        .show();
                button.setBackgroundColor(color);

            }else {
                //一定以下ならば，以下のダイアログ表示(ボタンの色は灰色のまま)
                new AlertDialog.Builder(this)
                        //.setTitle("title")
                        .setMessage("今日も1日頑張っていきましょう！")
                        .setPositiveButton("OK", null)
                        .show();
                button.setBackgroundColor(Color.GRAY);
            }
        }

        //画面のインスタンスを取得
        TextView textView= (TextView)findViewById(R.id.txt_count);
        //onCreateが呼ばれた回数を3桁で0埋めして表示用のTextViewに設定
        textView.setText(String.format("%1$03d", bootCount));
    }
    // 医師に相談ボタン押下時の処理
    //Gmailを開き(変更の可能性あり)，bootcountを0に，ボタンの色を灰色に，ボタンを押下不可にする
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
