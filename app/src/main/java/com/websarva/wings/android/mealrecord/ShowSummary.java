package com.websarva.wings.android.mealrecord;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.util.ArrayList;


public class ShowSummary extends AppCompatActivity {
    private LineChart mpLineChart;
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    SQLiteDatabase db;
    PainData alldata;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check= false;

    LineDataSet lineDataSet1;
    LineDataSet lineDataSet2;

    GraphSettings settings;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_summary);

        Resources res = getResources();
        int maincolor = res.getColor(R.color.colorMain);

        imgTrend = findViewById(R.id.img_trend);

        mpLineChart =(LineChart)findViewById(R.id.line_chart);

        String subject = "全てのデータ";

        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }


        settings= new GraphSettings(mpLineChart, maincolor, subject);

        settings.setXaxisDate(mpLineChart);


        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        ArrayList<ILineDataSet> dataSets;
        LineData data;

        settings.setXaxisDate(mpLineChart);

        check = checkAllEmpty();

        if(check){
            alldata = ListHandling.getAllList();
            if(alldata.value.length<1) {
                //Toast.makeText(ShowSummary.this,R.string.toast, Toast.LENGTH_SHORT).show();
                //reload();
                //break;
            }else{
                roc = TrendDefinition.calculateRateOfChange(alldata.value, alldata.value.length);
                trend =TrendDefinition.judgeTrend(roc);
            }
            TrendDefinition.showTrend(trend);

            Log.d("roc", String.valueOf(roc));
            Log.d("trend", String.valueOf(trend));

            lineDataSet1 = ListHandling.setList(alldata.date, alldata.value, false, mpLineChart);
            lineDataSet2 = ListHandling.setList(alldata.date, alldata.count,  true, mpLineChart);
            dataSets = new ArrayList<>();
            dataSets.add(lineDataSet1);
            dataSets.add(lineDataSet2);
            data = new LineData(dataSets);
            settings.setLinesAndPointsDetailsForValue(lineDataSet1,res);
            settings.setLinesAndPointsDetailsForCount(lineDataSet2,res);
            mpLineChart.setData(data);
            mpLineChart.invalidate();

        }else{
            Toast.makeText(ShowSummary.this,R.string.toast, Toast.LENGTH_SHORT).show();
        }

    }



    private  boolean checkAllEmpty(){
        check= false;
            Cursor cursor = ListHandling.getAllCursor();
            Log.d("count", String.valueOf(cursor.getCount()));
            if (cursor.getCount() >= 1) {
                check = true;
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

}

