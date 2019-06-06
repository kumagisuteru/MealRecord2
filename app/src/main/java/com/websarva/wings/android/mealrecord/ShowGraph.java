package com.websarva.wings.android.mealrecord;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.icu.text.AlphabeticIndex;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.websarva.wings.android.mealrecord.R;


import com.github.mikephil.charting.components.Legend;

import com.github.mikephil.charting.data.Entry;
/*
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
*/
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowGraph extends AppCompatActivity {

    //変数宣言
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    SQLiteDatabase db;

    private Spinner spCat;
    private String category = "満腹度";
    Integer valuearray[];
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);


        // 戻るボタン・グラフ領域のオブジェクト取得
        Button btnBack = findViewById(R.id.bt_return);
        btnBack.setOnClickListener(btnTap);
        Button btnShow = findViewById(R.id.btn_Show);
        btnShow.setOnClickListener(btnTap);

        mChart = findViewById(R.id.line_chart);


        // Grid背景色
        mChart.setDrawGridBackground(true);

        // no description text
        mChart.getDescription().setEnabled(true);

        // Grid縦軸を破線
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = mChart.getAxisLeft();
        // Y軸最大最小設定 今後変更の必要あり
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        // Grid横軸を破線
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);

        // add data
        //setData();

        mChart.animateX(2500);
        //mChart.invalidate();

        // dont forget to refresh the drawing
        // mChart.invalidate();



    }


    private View.OnClickListener btnTap = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (helper == null) {
                helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
            }

            if (db == null) {
                db = helper.getWritableDatabase();
            }

            //ボタンがタップされた時のそれぞれの処理をswitch文で記述
            //「送信」ならば、テキストボックスの文字列を取得し、データベースに保存
            switch (view.getId()) {
                case R.id.bt_return:
                    finish();
                    break;
                case R.id.btn_Show:
                    // レイアウトからSpinnerを取得
                    //spCat = findViewById(R.id.sp_Graph);
                    //Spinnerからカテゴリ名取得
                    //category = (String) spCat.getSelectedItem();
                    //当該カテゴリの要素を配列に代入
                    valuearray = getvaluelist(category);
                    //グラフ表示
                    setList(valuearray);
                    break;
            }
        }
    };



    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    /*
    //カテゴリ名を引数に、グラフに表示するデータを取得するメソッド
    public ArrayList<Date> getDatelist(String cat) {
        Log.d("debug", "**********Cursor");

        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)
        Cursor cursor = db.query(
                "mrdb",
                new String[]{"_id", "datetime", "category", "value"},
                "category=?",
                new String[]{cat},
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        // 忘れずに！
        //cursor.close();
        //
        String stringdate;
        Date date = new Date();
        ArrayList<Date> datelist = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            stringdate = cursor.getString(1);
            try {
                date = dateFormat.parse(stringdate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datelist.add(i, date);
            cursor.moveToNext();
        }


        Log.d("debug", "**********" + sbuilder.toString());
        //textView.setText(sbuilder.toString());

        return datelist;
    }
    */

    private Integer[] getvaluelist(String cat) {
        Integer valuearray[];
        Log.d("debug", "**********Cursor");

        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)
        Cursor cursor = db.query(
                "mrdb",
                new String[]{"_id", "datetime", "category", "value"},
                "category=?",
                new String[]{cat},
                null,
                null,
                null
        );

        cursor.moveToFirst();
        ArrayList<Integer> valuelist = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            valuelist.add(i, cursor.getInt(3));
            cursor.moveToNext();
        }
// 忘れずに！
        cursor.close();
        //
        valuearray=(Integer[])valuelist.toArray(new Integer[valuelist.size()]);
        return valuearray;
    }

    private void setList(Integer valuearray[]) {
// Entry()を使ってLineDataSetに設定できる形に変更してarrayを新しく作成
        mChart.notifyDataSetChanged();
        mChart.invalidate();

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < valuearray.length; i++) {
            values.add(new Entry(i, valuearray[i], null, null));
        }

        LineDataSet lineDataSet;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {

            lineDataSet = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            lineDataSet.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {

            // create a dataset and give it a type
            lineDataSet = new LineDataSet(values, category);

            lineDataSet.setDrawIcons(false);
            lineDataSet.setColor(Color.BLACK);
            lineDataSet.setCircleColor(Color.BLACK);
            lineDataSet.setLineWidth(1f);
            lineDataSet.setCircleRadius(3f);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setValueTextSize(0f);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFormLineWidth(1f);
            lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            lineDataSet.setFormSize(15.f);

            lineDataSet.setFillColor(Color.BLUE);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(lineDataSet); // add the datasets

            // create a data object with the datasets
            LineData lineData = new LineData(dataSets);

            // set data
            mChart.setData(lineData);
        }
    }
}