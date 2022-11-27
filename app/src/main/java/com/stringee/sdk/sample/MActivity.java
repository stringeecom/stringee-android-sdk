package com.stringee.sdk.sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressDialog prLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showProgress(String text) {
        prLoading = ProgressDialog.show(this, "", text);
        prLoading.setCancelable(true);
        prLoading.show();
    }

    public void dismissProgress() {
        if (prLoading != null && prLoading.isShowing()) {
            prLoading.dismiss();
        }
    }

    @Override
    public void onClick(View view) {

    }
}
