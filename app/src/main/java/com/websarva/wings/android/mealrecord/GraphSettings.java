package com.websarva.wings.android.mealrecord;

import android.content.res.Resources;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;

public class GraphSettings {
    protected static final long MILLIS_PER_1Day =86400000;

    /**
     * コンストラクタ
     * アプリの全グラフに共通する大枠の設定
     */
    public GraphSettings(LineChart lineChart, int color, String subject){
        setBackgrounds(lineChart);
        setLegends(lineChart, color);
        setYAxis(lineChart);
        setDescription(lineChart, subject);
    }

    /**
     * グラフの背景に関する設定
     */
    protected void setBackgrounds(LineChart lineChart){
        //背景色
        //lineChart.setBackgroundColor(Color.GRAY);

        //データがない時の表示とその文字の色
        lineChart.setNoDataText("No Data");
        lineChart.setNoDataTextColor(Color.BLUE);

        //グラフの格子の表示
        lineChart.setDrawGridBackground(true);

        //グラフの外枠を濃く(見やすく)表示
        lineChart.setDrawBorders(true);
        lineChart.setBorderColor(Color.GRAY);
        lineChart.setBorderWidth(3);

        //mpLineChart.setVisibleXRangeMaximum(4);
        //mpLineChart.setVisibleXRangeMinimum(2);
        //mpLineChart.moveViewToX(2);

    }

    /**
     * グラフの凡例に関する設定
     */
    protected void setLegends(LineChart lineChart, int color){
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(color);
        legend.setTextSize(15);
        //凡例のアイコンの変更
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        //凡例同士の間隔
        legend.setXEntrySpace(50);
        legend.setFormToTextSpace(10);
    }

    /**
     * グラフの右下に関する設定
     */
    protected void setDescription(LineChart lineChart, String subject){
        Description description = new Description();
        description.setText(subject);
        description.setTextColor(Color.BLUE);
        description.setTextSize(15);
        lineChart.setDescription(description);
    }

    /**
     * グラフの線と点に関する設定
     */
    protected void setLinesAndPointsDetailsForValue(LineDataSet lineDataSet, Resources res){
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
    protected void setLinesAndPointsDetailsForCount(LineDataSet lineDataSet, Resources res){

        /*****
         * グラフの線と点に関する設定
         */
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

    /**
     * X軸に関する設定
     */
    protected void setXaxisDate(LineChart lineChart){
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new MyAxisValueFormatterForMean());
    }
    protected void setXaxisMinute(LineChart lineChart){
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMinimum(-32500f);
        xAxis.setAxisMaximum(54200f);
        xAxis.setValueFormatter(new MyAxisValueFormatter());
    }
    protected void setYAxis(LineChart lineChart){
        YAxis yAxisLeft = lineChart.getAxisLeft();
        YAxis yAxisRight = lineChart.getAxisRight();

        //y軸の設定
        //yAxisLeft.setValueFormatter(new MyAxisValueFormatter());

        yAxisLeft.setAxisMaximum(100f);
        yAxisLeft.setAxisMinimum(0f);
        yAxisRight.setEnabled(false);
    }

    /**
     * データの数値の変更に関する設定
     * ミリ秒→HH/mm or MM/dd
     */
    protected static  class MyAxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //axis.setLabelCount(10, true);
            //sdfShort= new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat sdfhhmm = new SimpleDateFormat("HH:mm");
            //sdfhhmm = new SimpleDateFormat("HH:mm");
            value = value * 1000;
            String dateText = sdfhhmm.format(value);
            //String datetimeText = dateText + "\n" + sdfhhmm.format(value);
            return dateText;
            // return value + " $";

        }
    }
    protected static  class MyAxisValueFormatterForMean implements IAxisValueFormatter{
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            //axis.setLabelCount(10, true);
            SimpleDateFormat sdfmmdd = new SimpleDateFormat("MM/dd");
            value = value * MILLIS_PER_1Day;
            String dateText = sdfmmdd.format(value);
            //String datetimeText = dateText + "\n" + sdfhhmm.format(value);
            return dateText;

        }
    }


}