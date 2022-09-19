package com.smartcalculator.hcalculator.iap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.smartcalculator.hcalculator.R;
import com.smartcalculator.hcalculator.iap.utilies.Prefs;

public class IAPActivity extends AppCompatActivity {
    Prefs prefs;
    TextView txt_subscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iapactivity);
        txt_subscribed = findViewById(R.id.txt_subscribed);

        prefs = new Prefs(this);

        if (prefs.getPremium() == 1) {
            txt_subscribed.setText("You are a Premium Subscriber");
        } else {
            txt_subscribed.setText("You are not Subscribed");
        }

    }

    public void on_click_btn_subscribe(View view) {
        startActivity(new Intent(this, Subscriptions.class));
    }

    public void on_click_btn_buy_coins(View view) {
        startActivity(new Intent(this, BuyCoinActivity.class));
    }
}