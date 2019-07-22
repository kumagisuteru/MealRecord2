package com.websarva.wings.android.mealrecord;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.websarva.wings.android.mealrecord.ShowGraph.*;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ShowGraph extends AppCompatActivity {

    private LineChart mpLineChart;
    long millis;
    Integer minutes;
    Integer valuearray[];
    Integer datearray[];
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    SQLiteDatabase db;

    LineDataSet lineDataSet;

    private String category = "満腹度";

    SimpleDateFormat sdfLong;
    SimpleDateFormat sdfmmdd;
    SimpleDateFormat sdfhhmm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);


        mpLineChart =(LineChart)findViewById(R.id.line_chart);

        //dataSets.add(lineDataSet2);

        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        valuearray = getvaluelist(category);
        datearray = getDatelist(category);

        lineDataSet = setList(datearray, valuearray);


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);



        /*****

         グラフのスタイルのセッティング

         *****/
        //背景色
//        mpLineChart.setBackgroundColor(Color.GRAY);
        //データがない時の表示とその文字の色
        mpLineChart.setNoDataText("No Data");
        mpLineChart.setNoDataTextColor(Color.BLUE);

        //グラフの格子の表示
        mpLineChart.setDrawGridBackground(true);

        //グラフの外枠を濃く(見やすく)表示
        mpLineChart.setDrawBorders(true);
        mpLineChart.setBorderColor(Color.GRAY);
        mpLineChart.setBorderWidth(3);

        mpLineChart.setVisibleXRangeMaximum(4);
        //mpLineChart.setVisibleXRangeMinimum(2);
        mpLineChart.moveViewToX(2);

        /*****
         * グラフの線と点に関する設定
         */
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColor(Color.BLUE);
        lineDataSet.setCircleColorHole(Color.GRAY);
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(4);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setValueTextColor(Color.BLUE);
        //lineDataSet.enableDashedLine(5,10,0);
        //lineDataSet.setColors(colorArray, ShowGraph.this);

        /****
         * 凡例の設定
         */

        Legend legend = mpLineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLUE);
        legend.setTextSize(15);
        //凡例のアイコンの変更
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        //凡例同士の間隔
        legend.setXEntrySpace(50);
        legend.setFormToTextSpace(10);


        XAxis xAxis = mpLineChart.getXAxis();
        YAxis yAxisLeft = mpLineChart.getAxisLeft();
        YAxis yAxisRight = mpLineChart.getAxisRight();

        //x軸の設定
        xAxis.setValueFormatter(new MyAxisValueFormatter());
        //y軸の設定
        //yAxisLeft.setValueFormatter(new MyAxisValueFormatter());

        yAxisLeft.setAxisMaximum(100f);
        yAxisLeft.setAxisMinimum(0f);
        yAxisRight.setEnabled(false);

        /*****
         * グラフの右下のやつ
         *****/
        Description description = new Description();
        description.setText("...");
        description.setTextColor(Color.BLUE);
        description.setTextSize(15);
        mpLineChart.setDescription(description);

        LineData data = new LineData(dataSets);

        //プロットした点の表記を変える(全部)
        //data.setValueFormatter(new MyValueFormatter());
        //データセット1だけ
        lineDataSet.setValueFormatter(new MyValueFormatter());
        mpLineChart.setData(data);
        mpLineChart.invalidate();

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
                case R.id.btn_return:
                    finish();
                    break;
            }
        }
    };


    //カテゴリ名を引数に、グラフに表示するデータを取得するメソッド
    public Integer[] getDatelist(String cat) {
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
        sdfLong = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        // 忘れずに！
        //cursor.close();
        //
        String stringdate;
        Date date = new Date();
        ArrayList<Integer> datelist = new ArrayList<>();
        Integer datearray[];
        for (int i = 0; i < cursor.getCount(); i++) {
            stringdate = cursor.getString(1);
            try {
                date = sdfLong.parse(stringdate);
                //ミリ秒に変換
                millis = date.getTime();
                millis = millis / 60000;
                //分に変換
                minutes = (int)millis;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datelist.add(i, minutes);
            cursor.moveToNext();
        }


        Log.d("debug", "**********" + sbuilder.toString());
        //textView.setText(sbuilder.toString());

        datearray=(Integer[])datelist.toArray(new Integer[datelist.size()]);
        return datearray;
    }


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

    private class MyValueFormatter implements IValueFormatter{

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            int intvalue;
            intvalue = (int)value;
            String strvalue = String.valueOf(intvalue);
            return strvalue;
        }
    }

    private class MyAxisValueFormatter implements IAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //axis.setLabelCount(10, true);
            //sdfShort= new SimpleDateFormat("yyyy/MM/dd");
            sdfmmdd = new SimpleDateFormat("MM/dd");
            //sdfhhmm = new SimpleDateFormat("HH:mm");
            value = value * 60000;
            String dateText = sdfmmdd.format(value);
            //String datetimeText = dateText + "\n" + sdfhhmm.format(value);
            return dateText;
            // return value + " $";

        }
    }

    private LineDataSet setList(Integer datearray[], Integer valuearray[]) {
// Entry()を使ってLineDataSetに設定できる形に変更してarrayを新しく作成
        mpLineChart.notifyDataSetChanged();
        mpLineChart.invalidate();

        ArrayList<Entry> values = new ArrayList<>();
        LineDataSet lineDataSet;

        for (int i = 0; i < valuearray.length; i++) {
            values.add(new Entry(datearray[i], valuearray[i], null, null));
        }
        lineDataSet = new LineDataSet(values, category);

        return lineDataSet;
    }
}