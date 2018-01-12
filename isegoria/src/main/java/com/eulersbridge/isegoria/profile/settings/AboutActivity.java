package com.eulersbridge.isegoria.profile.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.eulersbridge.isegoria.R;

public class AboutActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());
    }
}
