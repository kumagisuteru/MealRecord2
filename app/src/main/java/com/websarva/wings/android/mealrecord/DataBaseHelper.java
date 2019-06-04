
package com.websarva.wings.android.mealrecord;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//データベースの変更を行いたいときに編集

public class DataBaseHelper extends SQLiteOpenHelper {

    /*
    データベースファイル名の定数フィールド
     */

    private static final String DATABASE_NAME = "MealRecord.db";
    private static final String TABLE_NAME = "mrdb";
    private static final String _ID = "_id";
    private static final String DATETIME = "datetime";
    private static final String COLUMN_NAME_TITLE = "category";
    private static final String COLUMN_NAME_SUBTITLE = "value";



    /*
    バージョン情報の定数フィールド
     */

    private static final int DATABASE_VERSION = 1;

    /*
    コンストラクタ
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        db.execSQL(
                SQL_CREATE_ENTRIES
        );

        Log.d("debug", "onCreate(SQLiteDatabase db)");

    }
    public DataBaseHelper(Context context){
        //親クラスのコンストラクタの呼び出し
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    DATETIME +" TEXT, " +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_SUBTITLE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

