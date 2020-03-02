/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.playassetdeliverynative;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NativeActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TeapotNativeActivity extends NativeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide toolbar
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT >= 19) {
            setImmersiveSticky();

            View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            setImmersiveSticky();
                        }
                    });
        }
    }

    @TargetApi(19)
    protected void onResume() {
        super.onResume();

        //Hide toolbar
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT >= 11 && SDK_INT < 14) {
            getWindow().getDecorView().setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
        } else if (SDK_INT >= 14 && SDK_INT < 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else if (SDK_INT >= 19) {
            setImmersiveSticky();
        }

    }
    // Our popup window, you will call it from your C/C++ code later

    @TargetApi(19)
    void setImmersiveSticky() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    TeapotNativeActivity _activity;
    PopupWindow _popupWindow;
    TextView _label;
    TextView _info;

    @SuppressLint("InflateParams")
    public void showUI() {
        if (_popupWindow != null)
            return;

        _activity = this;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.widgets, null);
                _popupWindow = new PopupWindow(
                        popupView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);

                LinearLayout mainLayout = new LinearLayout(_activity);
                MarginLayoutParams params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 0);
                _activity.setContentView(mainLayout, params);

                // Show our UI over NativeActivity window
                _popupWindow.showAtLocation(mainLayout, Gravity.TOP | Gravity.START, 10, 10);
                _popupWindow.update();

                _label = popupView.findViewById(R.id.textView);
                _label.setText("Double Tap Right Bottom Corner To Change Texture");

                _info = popupView.findViewById(R.id.infoView);
            }
        });
    }

    protected void onPause() {
        super.onPause();
    }

    //Log info on top of screen
    public void logHeader(final char[] str) {
        if (_label == null)
            return;

        _activity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _label.setText(str, 0, str.length);
            }
        });
    }

    //Log info on bottom of screen
    public void logInfo(final char[] str) {
        if (_info == null)
            return;

        _activity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _info.setText(str, 0, str.length);
            }
        });
    }

    public int updateButtons() {
        int returnCode = buttonCode;
        buttonCode = 0;
        return returnCode;
    }

    int buttonCode = 0;

    public void onClickPack1Btn(View v) {
        buttonCode = 1;
    }

    public void onClickPack2Btn(View v) {
        buttonCode = 2;
    }

    public void onClickPack3Btn(View v) {
        buttonCode = 3;
    }

    public void onClickRequestInfoBtn(View v) {
        buttonCode = 4;
    }

    public void onClickRequestBtn(View v) {
        buttonCode = 5;
    }

    public void onClickPauseBtn(View v) {
        buttonCode = 6;
    }

    public void onClickResumeBtn(View v) {
        buttonCode = 7;
    }

    public void onClickPrintLocationBtn(View v) {
        buttonCode = 8;
    }

    public void onClickShowCellularBtn(View v) {
        buttonCode = 9;
    }
}


