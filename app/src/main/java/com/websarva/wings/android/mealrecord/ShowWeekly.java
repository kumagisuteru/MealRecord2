package com.websarva.wings.android.mealrecord;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static com.websarva.wings.android.mealrecord.ShowGraph.imgTrend;

public class ShowWeekly extends AppCompatActivity {
    private LineChart mpLineChart;
    com.websarva.wings.android.mealrecord.DataBaseHelper helper;
    SQLiteDatabase db;
    PainData oneweek;

    int trend;
    double roc;
    ImageView imgTrend;
    private boolean check = false;

   // LineDataSet lineDataSet1;
  //  LineDataSet lineDataSet2;

    GraphSettings settings;
    private Intent intent;
    private Spinner spYear;
    private Spinner spMonth;
    private Spinner spWeek;
    Resources res;
    protected static int iYear;
    protected static int iMonth;
    protected static int iWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weekly);

        res = getResources();
        int maincolor = res.getColor(R.color.colorMain);
        imgTrend = findViewById(R.id.img_trend);
        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        Button btnShow = findViewById(R.id.btn_show);
        btnShow.setOnClickListener(btnTap);

        String subject = "指定した1週間のデータ";

        if (helper == null) {
            helper = new com.websarva.wings.android.mealrecord.DataBaseHelper(getApplicationContext());
        }

        if (db == null) {
            db = helper.getWritableDatabase();
        }

        settings = new GraphSettings(mpLineChart, maincolor, subject);

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
            switch (view.getId()) {
                case R.id.btn_show:
                    settings.setXaxisDate(mpLineChart);
                    spYear = findViewById(R.id.sp_year);
                    String strYear = (String) spYear.getSelectedItem();
                    iYear = Integer.parseInt(strYear);
                    Log.d("year", String.valueOf(iYear));
                    spMonth = findViewById(R.id.sp_month);
                    String strMonth = (String) spMonth.getSelectedItem();
                    iMonth = Integer.parseInt(strMonth);
                    Log.d("month", String.valueOf(iMonth));
                    spWeek = findViewById(R.id.sp_week);
                    String strDate = (String) spWeek.getSelectedItem();
                    iWeek = Integer.parseInt(strDate.substring(0, 1));
                    Log.d("date", String.valueOf(iWeek));
                    check = checkOneWeekEmpty(iYear, iMonth, iWeek);

                    if (check) {
                        oneweek = ListHandling.getOneWeekList(iYear, iMonth, iWeek);
                        if (oneweek.value.length < 1) {
                            Toast.makeText(ShowWeekly.this, R.string.toast, Toast.LENGTH_SHORT).show();
                            reload();
                            break;
                        } else {
                            roc = TrendDefinition.calculateRateOfChange(oneweek.value, oneweek.value.length);
                            trend = TrendDefinition.judgeTrend(roc);
                        }
                        TrendDefinition.showTrend(trend,imgTrend);

                        Log.d("roc", String.valueOf(roc));
                        Log.d("trend", String.valueOf(trend));

                        LineDataSet lineDataSet1 = ListHandling.setList(oneweek.date, oneweek.value, false, mpLineChart);
                        LineDataSet lineDataSet2 = ListHandling.setList(oneweek.date, oneweek.count,  true, mpLineChart);
                        dataSets = new ArrayList<>();
                        dataSets.add(lineDataSet1);
                        dataSets.add(lineDataSet2);
                        data = new LineData(dataSets);
                        settings.setLinesAndPointsDetailsForValue(lineDataSet1, res);
                        settings.setLinesAndPointsDetailsForCount(lineDataSet2, res);
                        mpLineChart.setData(data);
                        mpLineChart.invalidate();

                    } else {
                        Toast.makeText(ShowWeekly.this, R.string.toast, Toast.LENGTH_SHORT).show();
                        reload();
                    }
                    break;
            }
        }
    };

    protected boolean checkOneWeekEmpty(int year, int month, int week) {
        check= false;
            Cursor cursor = ListHandling.getOneWeekCursor(year, month, week);
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

