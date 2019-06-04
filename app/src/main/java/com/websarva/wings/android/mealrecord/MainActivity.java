package com.websarva.wings.android.mealrecord;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.location.LocationListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //変数宣言
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    private SQLiteDatabase db;
    private Date date;
    private String stdate;
    private String category;
    private int score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //ここから追記
        //レイアウトアイテム取得
        final SeekBar seekM = (SeekBar) findViewById(R.id.seekBar_Meal);
        final TextView Mscoretex = (TextView) findViewById(R.id.Meal_score);
        final ImageView faceScaleImageView = (ImageView) findViewById(R.id.Face_image);

        Button btrecord =findViewById(R.id.bt_record);
        Button btList =findViewById(R.id.bt_List);

        //データベース取得
        helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        //シークバー
        Mscoretex.setText("満腹度:"+seekM.getProgress());

        seekM.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        //ツマミをドラッグしたときに呼ばれる

                        int Mscore;
                        Mscore = seekM.getProgress();

                        if(Mscore >= 0 && Mscore < 20){
                            faceScaleImageView.setImageResource(R.drawable.painface1);

                        }else if(Mscore >= 20 && Mscore < 40){
                            faceScaleImageView.setImageResource(R.drawable.painface2);

                        }else if(Mscore >= 40 && Mscore < 60){
                            faceScaleImageView.setImageResource(R.drawable.painface3);

                        }else if(Mscore >= 60 && Mscore < 80){
                            faceScaleImageView.setImageResource(R.drawable.painface4);

                        }else if(Mscore >= 80 && Mscore <= 100){
                            faceScaleImageView.setImageResource(R.drawable.painface5);

                        }


                        Mscoretex.setText("満腹度:"+seekM.getProgress());
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
        RecordconfirmDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OKボタン押下時の処理
               // Log.d("AlertDialog", "Positive which :" + which);

                //OKボタン押下時の日付取得
                date = new Date();
                //日付表記を変更
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                //string型に変換
                stdate = sdf.format(date);
                //categoryに満腹度を代入
                //複数のカテゴリがある場合、レイアウト等から該当するカテゴリを取ってきて代入
                category = "満腹度";
                //シークバーの数値を取ってくる
                score = seekM.getProgress();
                //日付時刻と文字列をデータベースに記録
                insertData(db, stdate, category, score);

                //完了画面表示
                Intent intent = new Intent(getApplication(), Recordfin_Activity.class);
                startActivity(intent);
            }
        });

        // NG(否定的な)ボタンの設定
        RecordconfirmDialog.setNegativeButton("NG", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NGボタン押下時の処理
               // Log.d("AlertDialog", "Negative which :" + which);
            }
        });


        btrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecordconfirmDialog.show();


            }
        });

        btList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), ShowList.class);
                startActivity(intent);
            }
        });

    }

    //データベースに記録する関数
    //引数は データベース,日付文字列,カテゴリ,数値
    public void insertData(SQLiteDatabase db, String dat, String cat, int val) {

        ContentValues values = new ContentValues();
        values.put("datetime", dat);
        values.put("category", cat);
        values.put("value", val);

        //mrdbという名前のデータベースに保存
        db.insert("mrdb", null, values);
    }
}