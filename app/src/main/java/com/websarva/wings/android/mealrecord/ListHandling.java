package com.websarva.wings.android.mealrecord;

import android.database.Cursor;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.websarva.wings.android.mealrecord.ShowGraph.db;
/*****
 *
 * グラフに使うデータを操作するクラス
 *
 */

public class ListHandling {
    protected static final long MILLIS_PER_1Day =86400000;

    protected static Cursor getOnedayCursor(int year, int month, int date) {
        Cursor cursor = db.query(
                "paindb",
                new String[]{"year", "month", "date", "time", "value"},
                "year=? AND month=? AND date=? AND value > 0",
                new String[]{String.valueOf(year) , String.valueOf(month), String.valueOf(date)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor;
    }
    protected static PainData getOnedayList(int year, int month, int date){

        ArrayList<Integer> valuelist = new ArrayList<>();
        ArrayList<Integer> datelist = new ArrayList<>();
        Integer valuearray[];
        Integer datearray[];
        long millis;
        int minutes=0;
        Date onedate;
        SimpleDateFormat sdfhms = new SimpleDateFormat("HH:mm:ss");

        Cursor cursor = getOnedayCursor(year, month, date);

        for (int i = 0; i < cursor.getCount(); i++) {
            try {
                onedate = sdfhms.parse(cursor.getString(3));
                //HH:mm:ssをミリ秒に変換
                millis = onedate.getTime();
                //桁が多すぎるのでミリ秒→秒へ
                millis = millis / 1000;
                //分に変換
                minutes = (int)millis;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            datelist.add(minutes);
            valuelist.add(cursor.getInt(4));
            cursor.moveToNext();
        }
        cursor.close();
        //リストを配列に変換
        valuearray=(Integer[])valuelist.toArray(new Integer[valuelist.size()]);
        datearray=(Integer[])datelist.toArray(new Integer[datelist.size()]);

        PainData onedaydata = new PainData();
        onedaydata.value=valuearray;
        onedaydata.date=datearray;
        return onedaydata;
    }

    protected static Cursor getOneWeekCursor(int year, int month, int week){
        Cursor cursor = db.query(
                "paindb",
                new String[]{"year", "month", "date", "value"},
                "year=? AND month=? AND date>=? AND date<=? AND value > 0",
                new String[]{String.valueOf(year) , String.valueOf(month), String.valueOf(week*7-6), String.valueOf(week*7)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor;
    }
    protected static PainData getOneWeekList(int year, int month, int week){

        Cursor cursor = getOneWeekCursor(year,month, week);
        PainData oneweek = getMeanGraphData(cursor);
        return oneweek;
    }

    protected static Cursor getLastWeekCursor(int year, int month, int date){
        Cursor cursor;
        /**
         * 過去1週間分のデータを取得する際，月をまたぐ必要がある場合
         */
        if (date < 7){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            /**
             monthは(1月から0始まり，つまり12月は11)
             指定月の1月前なのでmonth-2
             */
            calendar.set(Calendar.MONTH, month - 2);
            int lastday = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            int predate= lastday + date - 6;

            cursor = db.query(
                    "paindb",
                    new String[]{"year", "month", "date", "value"},
                    "(year=? AND month=? AND date<=? AND value > 0) OR (year=? AND month=? AND date>=? AND value > 0)",
                    new String[]{String.valueOf(year) , String.valueOf(month),  String.valueOf(date),String.valueOf(year) , String.valueOf(month-1),  String.valueOf(predate)},
                    null,
                    null,
                    null
            );
        }else {
            cursor = db.query(
                    "paindb",
                    new String[]{"year", "month", "date", "value"},
                    "year=? AND month=? AND date>=? AND date<=? AND value > 0",
                    new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(date - 6), String.valueOf(date)},
                    null,
                    null,
                    null
            );
        }
        cursor.moveToFirst();
        return cursor;
    }
    protected static PainData getLastWeekList(int year, int month, int date){
        Cursor cursor = getLastWeekCursor(year, month, date);
        PainData lastweek = getMeanGraphData(cursor);
        return lastweek;
    }

    protected static Cursor getAllCursor(){
        Cursor cursor = db.query(
                "paindb",
                new String[]{"year", "month", "date", "value"},
                " value > 0",
                null,
                null,
                null,
                null
        );
        return cursor;
    }
    protected static PainData getAllList() {
        //query(テーブル名, 取得するレコード, WHERE句, WHERE句の指定の値,
        // GROUP BY句 同じ値を持つデータでグループ化,
        // HAVING句 WHERE句のグループ版, ORDER BY句 並び順)
        Cursor cursor = getAllCursor();
        PainData allmean = getMeanGraphData(cursor);
        return allmean;

    }

    private static PainData getMeanGraphData(Cursor cursor){
        Integer meanarray[] ,datearray[], countarray[];;
        String strdatearray[];
        int tempmonth, tempdate, nextmonth, nextdate;
        Integer tempvalue, tempcount=0;
        ArrayList<Integer> templist = new ArrayList<>();
        ArrayList<Integer> meanlist = new ArrayList<>();
        ArrayList<String> datelist = new ArrayList<>();
        ArrayList<Integer> countlist = new ArrayList<>();

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            tempcount++;
            tempmonth = cursor.getInt(1);
            tempdate = cursor.getInt(2);
            templist.add(cursor.getInt(3));
            cursor.moveToNext();
            if(i >= cursor.getCount()-1){
                break;
            }
            nextmonth = cursor.getInt(1);
            nextdate =cursor.getInt(2);
            if(tempmonth == nextmonth &&tempdate == nextdate){
                continue;
            }else {
                tempvalue = mean(templist);
                meanlist.add(tempvalue);
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

        //リストを配列に変換
        meanarray=(Integer[])meanlist.toArray(new Integer[meanlist.size()]);
        strdatearray=(String[])datelist.toArray(new String[datelist.size()]);
        datearray = convertStrDatetoInt(strdatearray);
        countarray = (Integer[])countlist.toArray(new Integer[countlist.size()]);

        PainData meanData = new PainData();
        meanData.value=meanarray;
        meanData.date=datearray;
        meanData.count=countarray;

        return meanData;
    }

    private static Integer[] convertStrDatetoInt(String[] strdate){
        Integer intDate[];
        Integer imillis=0;
        Date date = new Date();
        long millis;
        SimpleDateFormat sdfmmdd = new SimpleDateFormat("MM/dd");
        List<String> list = Arrays.asList(strdate);
        ArrayList<Integer> datelist = new ArrayList<>();
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

    protected  static LineDataSet setList(Integer datearray[], Integer valuearray[], boolean count, LineChart lineChart) {
        // Entry()を使ってLineDataSetに設定できる形に変更してarrayを新しく作成
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        ArrayList<Entry> values = new ArrayList<>();
        LineDataSet lineDataSet;

        for (int i = 0; i < valuearray.length; i++) {
            values.add(new Entry(datearray[i], valuearray[i], null, null));
            Log.d("value", String.valueOf(valuearray[i]));
        }

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            lineDataSet.setValues(values);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else if (!count){
            // create a dataset and give it a type
            lineDataSet = new LineDataSet(values, "痛みの強さ");

        } else {
            lineDataSet = new LineDataSet(values, "記録回数");
        }
        lineDataSet.setValueFormatter(new MyValueFormatter());
        return lineDataSet;
    }


    protected static  class MyValueFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            int intvalue;
            intvalue = (int)value;
            String strvalue = String.valueOf(intvalue);
            return strvalue;
        }
    }

}