package com.websarva.wings.android.mealrecord;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowList extends AppCompatActivity {

    //変数宣言
    private EditText editText;
    private ListView listView;
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    private SQLiteDatabase db;
    private String category;
    private String  delId;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        //レイアウト取得
        Button btnBack = findViewById(R.id.bt_return);
        btnBack.setOnClickListener(btnTap);
        Button btnDelete = findViewById(R.id.bt_delete);
        btnDelete.setOnClickListener(btnTap);
        listView = findViewById(R.id.list_view);
        editText = findViewById(R.id.et_Num);

        //データベース取得
        helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(this);
        db = helper.getReadableDatabase();

        //画面遷移時に表示するために、onCreate内でデータベースの表示を行う
        readData("満腹度");


    }

    private View.OnClickListener btnTap = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            //データベースが存在していなかったら作る？
            if (helper == null) {
                helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
            }

            if (db == null) {
                db = helper.getWritableDatabase();
            }

            //ボタン押下時の処理
            switch (view.getId()) {
                //戻るボタン
                case R.id.bt_return:
                    finish();
                    break;

                //削除ボタンの処理
                case R.id.bt_delete:
                    //テキストボックスの数値を取得
                    delId = editText.getText().toString();
                    //テキストボックスに入力された数値に該当するIDのデータを削除
                    db.delete("mrdb", "_id = ?", new String[]{delId});
                    //画面の更新をする
                    reload();
                    break;
            }
        }
    };

    //データベースを読み込み文字列変換し表示する関数
    public void readData(String cat){

        Log.d("debug","**********Cursor");

        //データの参照はカーソルCursorを用いる

        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)

        //引数のカテゴリ名を基にデータベース内のデータを抽出する
        Cursor cursor = db.query(
                "mrdb",
                new String[] { "_id","datetime", "category", "value" },
                //new String[] {"datetime", "category", "value" },
                "category=?",
                //"category=?",
                new String[]{cat},
                //new String[]{"Satisfaction"},
                null,
                null,
                null
        );

        //カーソルを一番上に戻す
        cursor.moveToFirst();

        //StringBuilder sbuilder = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        String date;

        //ArrayList data = new ArrayList<>();
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        //抽出したデータのも実を表示するための変数に格納
        //表示形式↓↓↓
        // ID: 年/日/月 時:分:秒: カテゴリ名: 値
        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, String> item = new HashMap<String, String>();
            sb1.append(cursor.getString(2));
            sb1.append(": ");
            sb1.append(cursor.getInt(3));
            date = cursor.getString(1);
            item.put("Subject", sb1.toString());
            item.put("Comment", date);
            data.add(item);
            sb1.delete(0, sb1.length());
            cursor.moveToNext();


            /*
            sbuilder.append(cursor.getInt(0));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(1));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(2));
            sbuilder.append(": ");
            sbuilder.append(cursor.getInt(3));
            cursor.moveToNext();
            data.add(sbuilder.toString());
            sbuilder.delete(0, sbuilder.length());
            */
        }

        // 忘れずに！
        cursor.close();

        /*
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        Log.d("debug","**********"+sbuilder.toString());
        */
        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                android.R.layout.simple_list_item_2,
                new String[] { "Subject", "Comment" },
                new int[] { android.R.id.text1, android.R.id.text2});

        //リストビューに文字列代入
        listView.setAdapter(adapter);
        //listView.setText(sbuilder.toString());
    }

    //画面を再読み込みする関数
    private void reload() {
        intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }
}
