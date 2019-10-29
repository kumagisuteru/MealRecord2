package com.websarva.wings.android.mealrecord;

import android.util.Log;

import static com.websarva.wings.android.mealrecord.ShowGraph.imgTrend;

public class TrendDefinition {
    protected static final int INCREASE_TREND =1;
    protected static final int STAGNATION_TREND =0;
    protected static final int DECREMENT_TREND =-1;
    protected static final double BORDER_OF_TREND = 0.2;


    protected static void showTrend(int trend){
        if(trend == INCREASE_TREND){
            imgTrend.setImageResource(R.drawable.increse);
        }else if(trend == STAGNATION_TREND){
            imgTrend.setImageResource(R.drawable.stagnation);
        }else{
            imgTrend.setImageResource(R.drawable.decrese);
        }
    }

    protected static double calculateRateOfChange(Integer[] array, int length){
        double rateofchange=0;
        Log.d("rateofchange", String.valueOf(rateofchange));
        if(length == 1){
            rateofchange = 0;
        }else if(length < 5) {
            for (int i = length - 1; i > 0; i--) {
                Log.d("rateofchange", String.valueOf(rateofchange));
                rateofchange += (double) (array[i] - array[i - 1]) / array[i - 1];
            }
        } else{
            for(int i = length-1; i>length - 5; i--){
                rateofchange += (double)(array[i]-array[i-1])/array[i-1];
                Log.d("rateofchange", String.valueOf(rateofchange));
            }
        }
        return rateofchange;
    }

    protected static int judgeTrend(double roc){
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

