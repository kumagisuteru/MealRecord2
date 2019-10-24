package com.websarva.wings.android.mealrecord;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ShowGraph extends AppCompatActivity {

    final long MILLIS_PER_1Day = 86400000;
    final int INCREASE_TREND =1;
    final int STAGNATION_TREND =0;
    final int DECREMENT_TREND =-1;

    final double BORDER_OF_TREND = 0.2;
    private LineChart mpLineChart;
    long millis;
    Integer minutes;
    Integer valuearray[];
    Integer datearray[];
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    SQLiteDatabase db;
    ValueDate meandate;
    ValueDate oneday;

    LineDataSet lineDataSet1;
    LineDataSet lineDataSet2;

    private Intent intent;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check= false;

    SimpleDateFormat sdfLong;
    SimpleDateFormat sdfmmdd;
    SimpleDateFormat sdfhhmm;

    private Spinner spYear;
    private Spinner spMonth;
    private Spinner spDate;

    private int iYear;
    private int iMonth;
    private int iDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        // 戻るボタン・グラフ領域のオブジェクト取得
        Button btnSummary = findViewById(R.id.btn_summary);
        btnSummary.setOnClickListener(btnTap);
        Button btnShow = findViewById(R.id.btn_show);
        btnShow.setOnClickListener(btnTap);

        Resources res = getResources();
        int maincolor = res.getColor(R.color.colorMain);

        imgTrend = findViewById(R.id.img_trend);

        mpLineChart =(LineChart)findViewById(R.id.line_chart);

        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }


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

        //mpLineChart.setVisibleXRangeMaximum(4);
        //mpLineChart.setVisibleXRangeMinimum(2);
        //mpLineChart.moveViewToX(2);


        /****
         * 凡例の設定
         */

        Legend legend = mpLineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(maincolor);
        legend.setTextSize(15);
        //凡例のアイコンの変更
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        //凡例同士の間隔
        legend.setXEntrySpace(50);
        legend.setFormToTextSpace(10);

        YAxis yAxisLeft = mpLineChart.getAxisLeft();
        YAxis yAxisRight = mpLineChart.getAxisRight();


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

            ArrayList<ILineDataSet> dataSets;
            LineData data;
            XAxis xAxis = mpLineChart.getXAxis();
            //ボタンがタップされた時のそれぞれの処理をswitch文で記述
            //「送信」ならば、テキストボックスの文字列を取得し、データベースに保存
            switch (view.getId()) {

                case R.id.btn_summary:
                    //reload();
                    intent = new Intent(getApplication(), ShowSummary.class);
                    startActivity(intent);
                    break;

                case R.id.btn_show:
                    xAxis = mpLineChart.getXAxis();
                    xAxis.setAxisMinimum(-32500f);
                    xAxis.setAxisMaximum(54200f);
                    xAxis.setValueFormatter(new MyAxisValueFormatter());
                    spYear = findViewById(R.id.sp_year);
                    String strYear = (String)spYear.getSelectedItem();
                    iYear = Integer.parseInt(strYear);
                    //Log.d("year", String.valueOf(iYear));
                    spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String)spMonth.getSelectedItem();
                    iMonth = Integer.parseInt(strMonth);
                    //Log.d("month", String.valueOf(iMonth));
                    spDate = findViewById(R.id.sp_date);
                    String strDate = (String)spDate.getSelectedItem();
                    iDate = Integer.parseInt(strDate);
                    //Log.d("date", String.valueOf(iDate));
                    check = checkEmpty(iYear, iMonth, iDate,false);

                    if(check){
                        oneday = getOnedayList(iYear, iMonth, iDate);
                        if(oneday.date.length<1) {
                            Toast.makeText(ShowGraph.this,R.string.toast, Toast.LENGTH_SHORT).show();
                            reload();
                            break;
                        }else{
                            roc = calculateRateOfChange(oneday.value, oneday.value.length);
                            trend =judgeTrend(roc);
                        }
                        showTrend(trend);
                        Log.d("trend", String.valueOf(trend));
                        //Log.d("oneday", "ok");
                        lineDataSet1 = setList(oneday.date, oneday.value, 1,false);
                        //Log.d("linedataset", "ok");
                        dataSets = new ArrayList<>();
                        dataSets.add(lineDataSet1);
                        //Log.d("datasets.add", "ok");
                        data = new LineData(dataSets);
                        //Log.d("data", "ok");
                        lineDataSet1.setValueFormatter(new MyValueFormatter());
                        setLinesAndPointsDetails(lineDataSet1);
                        //Log.d("setvalue", "ok");
                        mpLineChart.setData(data);
                        //Log.d("setdata", "ok");
                        mpLineChart.invalidate();
                    }else{
                        Toast.makeText(ShowGraph.this,R.string.toast, Toast.LENGTH_SHORT).show();
                        reload();
                    }
                    break;
            }
        }
    };

    private ValueDate getOnedayList(int year, int month, int date){

        ArrayList<Integer> valuelist = new ArrayList<>();
        ArrayList<Integer> datelist = new ArrayList<>();

        Cursor cursor = db.query(
                "paindb",
                new String[]{"year", "month", "date", "time", "value"},
                "year=? AND month=? AND date=?",
                new String[]{String.valueOf(year) , String.valueOf(month), String.valueOf(date)},
                null,
                null,
                null
        );

        cursor.moveToFirst();

        sdfLong = new SimpleDateFormat("HH:mm:ss");

        // 忘れずに！
        //cursor.close();
        //


        Date onedate = new Date();
        if(cursor.getCount() < 1){
            finish();
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            try {
                onedate = sdfLong.parse(cursor.getString(3));
                Log.d("strdate", cursor.getString(3));
                //ミリ秒に変換
                millis = onedate.getTime();
                //millis+= 32400000;
                millis = millis / 1000;
                //分に変換
                minutes = (int)millis;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datelist.add(minutes);
            valuelist.add(cursor.getInt(4));
            Log.d("intdate", String.valueOf(datelist.get(i)));
            Log.d("value", String.valueOf(valuelist.get(i)));
            cursor.moveToNext();
        }
        cursor.close();
        //リストを配列に変換
        valuearray=(Integer[])valuelist.toArray(new Integer[valuelist.size()]);
        datearray=(Integer[])datelist.toArray(new Integer[datelist.size()]);

        ValueDate onedaydata = new ValueDate();
        onedaydata.value=valuearray;
        onedaydata.date=datearray;

        return  onedaydata;
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

    private class ValueDate{
        Integer date[];
        Integer value[];
        Integer count[];
    }

    private class MyAxisValueFormatter implements IAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //axis.setLabelCount(10, true);
            //sdfShort= new SimpleDateFormat("yyyy/MM/dd");
            sdfhhmm = new SimpleDateFormat("HH:mm");
            //sdfhhmm = new SimpleDateFormat("HH:mm");
            value = value * 1000;
            String dateText = sdfhhmm.format(value);
            //String datetimeText = dateText + "\n" + sdfhhmm.format(value);
            return dateText;
            // return value + " $";

        }
    }


    private LineDataSet setList(Integer datearray[], Integer valuearray[], int flag, boolean count) {
// Entry()を使ってLineDataSetに設定できる形に変更してarrayを新しく作成
        mpLineChart.notifyDataSetChanged();
        mpLineChart.invalidate();

        ArrayList<Entry> values = new ArrayList<>();
        LineDataSet lineDataSet;

        for (int i = 0; i < valuearray.length; i++) {
            values.add(new Entry(datearray[i], valuearray[i], null, null));
        }

        if (mpLineChart.getData() != null &&
                mpLineChart.getData().getDataSetCount() > 0) {

            lineDataSet = (LineDataSet) mpLineChart.getData().getDataSetByIndex(0);
            lineDataSet.setValues(values);
            mpLineChart.getData().notifyDataChanged();
            mpLineChart.notifyDataSetChanged();

        } else if (flag ==0 && !count){
            // create a dataset and give it a type
            lineDataSet = new LineDataSet(values, "痛みの強さ");

        } else{
            lineDataSet = new LineDataSet(values, "痛みの強さ");
        }
        return lineDataSet;
    }

    /*****
     * グラフの線と点に関する設定
     */
    private void setLinesAndPointsDetails(LineDataSet lineDataSet){

        /*****
         * グラフの線と点に関する設定
         */
        Resources res = getResources();
        int maincolor = res.getColor(R.color.colorMain);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(maincolor);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColor(Color.GRAY);
        lineDataSet.setCircleColorHole(maincolor);
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(4);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setValueTextColor(maincolor);
        //lineDataSet.enableDashedLine(5,10,0);
        //lineDataSet.setColors(colorArray, ShowGraph.this);


    }


    private void showTrend(int trend){
        if(trend == INCREASE_TREND){
            imgTrend.setImageResource(R.drawable.increse);
        }else if(trend == STAGNATION_TREND){
            imgTrend.setImageResource(R.drawable.stagnation);
        }else{
            imgTrend.setImageResource(R.drawable.decrese);
        }
    }

    private  boolean checkEmpty(int year, int month, int date, boolean flag){
        if (flag) {
            Cursor cursor = db.query(
                    "paindb",
                    new String[]{"year", "month", "date", "value"},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            Log.d("count", String.valueOf(cursor.getCount()));
            if(cursor.getCount()>=1){
                check = true;
            }
        }else {
            Cursor cursor = db.query(
                    "paindb",
                    new String[]{"year", "month", "date", "value"},
                    "year=? AND month=? AND date=?",
                    new String[]{String.valueOf(year) , String.valueOf(month), String.valueOf(date)},
                    null,
                    null,
                    null
            );
            cursor.moveToFirst();
            Log.d("count", String.valueOf(cursor.getCount()));
            if (cursor.getCount() >= 1) {
                check = true;
            }
        }
        return check;
    }

    private void reload() {
        intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private double calculateRateOfChange(Integer[] array, int length){
        double rateofchange=0;
        if(length == 1){
            rateofchange = 0;
        }else if(length < 5) {
            for (int i = length - 1; i > 0; i--) {
                Log.d("i", String.valueOf(array[i]));
                Log.d("i-1", String.valueOf(array[i - 1]));

                rateofchange += (double) (array[i] - array[i - 1]) / array[i - 1];
                Log.d("rateofchange", String.valueOf(rateofchange));
            }
        } else{
            for(int i = length-1; i>length - 5; i--){
                Log.d("i", String.valueOf(array[i]));
                Log.d("i-1", String.valueOf(array[i-1]));

                rateofchange += (double)(array[i]-array[i-1])/array[i-1];
                Log.d("rateofchange", String.valueOf(rateofchange));
            }
        }
        return rateofchange;
    }

    private int judgeTrend(double roc){
        int trend;
        if(roc>BORDER_OF_TREND){
            trend = INCREASE_TREND;
        }else if(roc< BORDER_OF_TREND*(-1)){
            trend = DECREMENT_TREND;
        }else{
            trend = STAGNATION_TREND;
        }
        return trend;
    }

}
