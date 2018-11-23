package com.stringee.sdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luannguyen on 4/23/2016.
 */
public class LoginActivity extends MActivity {

    private EditText etUsername;
    private View btnLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isLogin = PrefUtils.getBoolean(Constant.LOGIN, false);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.et_username);

        etUsername.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                checkButtonLoginEnable();
            }
        });

        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (checkButtonLoginEnable()) {
                    String userId = etUsername.getText().toString().trim();
                    Intent intent = new Intent(this, StartActivity.class);
                    intent.putExtra(Constant.USER_NAME, userId);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    private boolean checkButtonLoginEnable() {
        if (etUsername.getText().toString().trim().length() > 0) {
            btnLogin.setBackgroundResource(R.drawable.btn_login_enable);
            btnLogin.setEnabled(true);
            return true;
        } else {
            btnLogin.setBackgroundResource(R.drawable.btn_login_disable);
            btnLogin.setEnabled(false);
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            boolean isHide = true;
            List<View> views = new ArrayList<View>();
            views.add(etUsername);
            for (View view : views) {
                int scrcoords[] = new int[2];
                view.getLocationOnScreen(scrcoords);
                float x = event.getRawX() + view.getLeft() - scrcoords[0];
                float y = event.getRawY() + view.getTop() - scrcoords[1];
                if (x > view.getLeft() && x < view.getRight() && y > view.getTop() && y < view.getBottom()) {
                    isHide = false;
                    break;
                }
            }
            if (isHide) {
                Utils.hideKeyboard(this);
            }
        }
        return ret;
    }
}
