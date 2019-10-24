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

public class ShowSummary extends AppCompatActivity {

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

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check= false;

    LineDataSet lineDataSet1;
    LineDataSet lineDataSet2;

    private Intent intent;

    SimpleDateFormat sdfLong;
    SimpleDateFormat sdfmmdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_summary);

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



        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        ArrayList<ILineDataSet> dataSets;
        LineData data;


        //x軸の設定
        XAxis xAxis = mpLineChart.getXAxis();
        xAxis.setValueFormatter(new MyAxisValueFormatterForMean());
        check = checkEmpty(0,0,0,true);
        if(check) {
            meandate = getMeanList();
            if (meandate.value.length < 1) {
                Toast.makeText(ShowSummary.this, R.string.toast, Toast.LENGTH_SHORT).show();
                reload();
                //break;
            } else {
                roc = calculateRateOfChange(meandate.value, meandate.value.length);
                trend = judgeTrend(roc);
            }

            showTrend(trend);
            lineDataSet1 = setList(meandate.date, meandate.value, 0, false);
            lineDataSet2 = setList(meandate.date, meandate.count, 0, true);
            dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            dataSets.add(lineDataSet2);
            data = new LineData(dataSets);
            lineDataSet1.setValueFormatter(new MyValueFormatter());
            lineDataSet2.setValueFormatter(new MyValueFormatter());
            setLinesAndPointsDetails(lineDataSet1);
            setLinesAndPointsDetails2(lineDataSet2);
            mpLineChart.setData(data);
            mpLineChart.invalidate();
        }else{
            Toast.makeText(ShowSummary.this,R.string.toast, Toast.LENGTH_SHORT).show();
            reload();
        }

    }


    private ValueDate getMeanList() {
        Integer meanarray[];
        String strdatearray[];
        Integer datearray[];
        Integer countarray[];
        int tempmonth;
        int tempdate;
        Integer tempvalue;
        Integer tempcount=0;

        ArrayList<Integer> templist = new ArrayList<>();
        ArrayList<Integer> meanlist = new ArrayList<>();
        ArrayList<String> datelist = new ArrayList<>();
        ArrayList<Integer> countlist = new ArrayList<>();

        Log.d("debug", "**********Cursor");

        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)
        Cursor cursor = db.query(
                "paindb",
                new String[]{"year", "month", "date", "value"},
                "value > 0",
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        Log.d("count", String.valueOf(cursor.getCount()));

        for (int i = 0; i < cursor.getCount(); i++) {
            tempcount++;
            tempmonth = cursor.getInt(1);
            tempdate = cursor.getInt(2);
            templist.add(cursor.getInt(3));
            //Log.d("date", String.valueOf(cursor.getInt(2)));
            //Log.d("value", String.valueOf(cursor.getInt(3)));
            cursor.moveToNext();
//            Log.d("date", String.valueOf(cursor.getInt(2)));
            //          Log.d("value", String.valueOf(cursor.getInt(3)));
            if(i >= cursor.getCount()-1){
                break;
            }
            //Log.d("i", String.valueOf(i));
            int nextdate =cursor.getInt(2);

            if(tempdate == nextdate){
                continue;
            }else {
                tempvalue = mean(templist);
                //Log.d("mean", String.valueOf(tempvalue));
                meanlist.add(tempvalue);
                //datelist.add(cursor.getInt(1)+"/"+cursor.getInt(2));
                datelist.add(tempmonth+"/"+tempdate);
                countlist.add(tempcount);
                templist.clear();
                tempcount=0;
            }
        }
        cursor.moveToPrevious ();
        tempvalue = mean(templist);
        meanlist.add(tempvalue);
        datelist.add(cursor.getInt(1)+"/"+cursor.getInt(2));
        countlist.add(tempcount);
        // 忘れずに！
        cursor.close();

        Log.d("last", String.valueOf(meanlist.get(meanlist.size()-1)));

        //リストを配列に変換
        meanarray=(Integer[])meanlist.toArray(new Integer[meanlist.size()]);
        strdatearray=(String[])datelist.toArray(new String[datelist.size()]);
        datearray = convertStrDatetoInt(strdatearray);
        countarray = (Integer[])countlist.toArray(new Integer[countlist.size()]);

        ValueDate meandate = new ValueDate();
        meandate.value=meanarray;
        meandate.date=datearray;
        meandate.count=countarray;

        return  meandate;

    }

    private static Integer mean(ArrayList<Integer> list){
        int mean;
        if(list.size()==0){
            return 0;
        }
        int sum = 0;
        for(int i=0; i<list.size(); i++){
            sum += list.get(i);
        }
        mean =  sum / list.size();
        return (Integer)mean;
    }

    private Integer[] convertStrDatetoInt(String[] strdate){
        Integer intDate[];
        Integer imillis=0;
        Date date = new Date();
        long millis;
        sdfmmdd = new SimpleDateFormat("MM/dd");
        List<String> list = Arrays.asList(strdate);
        ArrayList<Integer> datelist = new ArrayList<>();
        Log.d("list_0", String.valueOf(list.get(0)));
        for(int i=0; i<list.size(); i++){
            try{
                date = sdfmmdd.parse(list.get(i));
                //ミリ秒に変換
                millis = date.getTime();
                millis = millis / MILLIS_PER_1Day;
                imillis = (int)millis;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datelist.add(imillis);
        }
        intDate=(Integer[])datelist.toArray(new Integer[datelist.size()]);

        return intDate;
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

    private class MyAxisValueFormatterForMean implements IAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //axis.setLabelCount(10, true);
            sdfmmdd = new SimpleDateFormat("MM/dd");
            //sdfhhmm = new SimpleDateFormat("HH:mm");
            value = value * MILLIS_PER_1Day;
            String dateText = sdfmmdd.format(value);
            //String datetimeText = dateText + "\n" + sdfhhmm.format(value);
            return dateText;

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
            lineDataSet = new LineDataSet(values, "痛みの強さ平均");

        } else{
            lineDataSet = new LineDataSet(values, "記録回数");
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

    private void setLinesAndPointsDetails2(LineDataSet lineDataSet){

        /*****
         * グラフの線と点に関する設定
         */
        Resources res = getResources();
        int accentcolor = res.getColor(R.color.colorAccent);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(accentcolor);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColor(Color.GRAY);
        lineDataSet.setCircleColorHole(accentcolor);
        lineDataSet.setCircleRadius(5);
        lineDataSet.setCircleHoleRadius(4);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setValueTextColor(accentcolor);
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

