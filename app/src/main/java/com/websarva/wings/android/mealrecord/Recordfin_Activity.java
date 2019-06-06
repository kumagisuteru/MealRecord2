package com.websarva.wings.android.mealrecord;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Recordfin_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordfin_);

        //ここから追記


        //メッセージ表示
        final TextView recodthx =  (TextView) findViewById(R.id.Record_thxtex);

        //  if(Mscore >= 90){
        recodthx.setText(R.string.record_thxA);
        //  }

        //  if(Mscore >= 0 && Mscore < 90){
        //  recodthx.setText(R.string.record_thxB);
        //  }

        //戻るボタン
        Button returnButton = findViewById(R.id.btn_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });





    }
}
