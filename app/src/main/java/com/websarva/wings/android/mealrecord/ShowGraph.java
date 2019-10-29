package com.websarva.wings.android.mealrecord;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.util.ArrayList;
import java.util.Calendar;


public class ShowGraph extends AppCompatActivity {

    protected static  LineChart mpLineChart;
    protected static  com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    protected static  SQLiteDatabase db;
    protected static  PainData lastweek;
    protected int trend;
    protected double roc;
    protected static  ImageView imgTrend;
    protected   LineDataSet lineDataSet1;
    protected   LineDataSet lineDataSet2;
    protected static  Intent intent;
    protected static  boolean check= false;
    GraphSettings     settings;
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);

        // 戻るボタン・グラフ領域のオブジェクト取得
        Button btnSummary = findViewById(R.id.btn_summary);
        btnSummary.setOnClickListener(btnTap);
        Button btnWeekly = findViewById(R.id.btn_weekly);
        btnWeekly.setOnClickListener(btnTap);
        Button btnOneday = findViewById(R.id.btn_oneday);
        btnOneday.setOnClickListener(btnTap);

        String subject = "過去1週間のデータ";

        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);

        imgTrend = findViewById(R.id.img_trend);

        mpLineChart =(LineChart)findViewById(R.id.line_chart);

        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        settings= new GraphSettings(mpLineChart, maincolor, subject);

        settings.setXaxisDate(mpLineChart);

        Calendar calendar= Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        //monthは0~11 目的の月にするには+1する必要あり
        int month = calendar.get(Calendar.MONTH)+1;
        int date = calendar.get(Calendar.DATE);
        check = checkLastWeekEmpty(year, month, date);

        if(check){
            lastweek = ListHandling.getLastWeekList(year, month, date);
            if(lastweek.value.length<1) {
                //Toast.makeText(ShowGraph.this,R.string.toast, Toast.LENGTH_SHORT).show();
                //reload();
                //break;
            }else{
                roc = TrendDefinition.calculateRateOfChange(lastweek.value, lastweek.value.length);
                trend =TrendDefinition.judgeTrend(roc);
            }
            TrendDefinition.showTrend(trend);

            Log.d("roc", String.valueOf(roc));
            Log.d("trend", String.valueOf(trend));
            ArrayList<ILineDataSet> dataSets;
            LineData data;

            lineDataSet1 =ListHandling.setList(lastweek.date, lastweek.value, false, mpLineChart);
            lineDataSet2 = ListHandling.setList(lastweek.date, lastweek.count,  true, mpLineChart);
            dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            dataSets.add(lineDataSet2);
            data = new LineData(dataSets);
            settings.setLinesAndPointsDetailsForValue(lineDataSet1,res);
            settings.setLinesAndPointsDetailsForCount(lineDataSet2,res);
            mpLineChart.setData(data);
            mpLineChart.invalidate();

        }else{
            Toast.makeText(ShowGraph.this,R.string.toast, Toast.LENGTH_SHORT).show();
        }
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

            switch (view.getId()) {
                case R.id.btn_summary:
                    intent = new Intent(getApplication(), ShowSummary.class);
                    startActivity(intent);
                    break;
                case R.id.btn_weekly:
                    intent = new Intent(getApplication(), ShowWeekly.class);
                    startActivity(intent);
                    break;
                case R.id.btn_oneday:
                    intent = new Intent(getApplication(), ShowOneday.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    protected boolean checkLastWeekEmpty(int year, int month, int date){
        check= false;
            Cursor cursor = ListHandling.getLastWeekCursor(year,month,date);
            cursor.moveToFirst();
            Log.d("count", String.valueOf(cursor.getCount()));
            if (cursor.getCount() >= 1) {
                check = true;
            }
        return check;
    }

}
