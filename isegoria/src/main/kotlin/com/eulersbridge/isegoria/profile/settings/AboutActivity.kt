package com.eulersbridge.isegoria.profile.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.eulersbridge.isegoria.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        backButton.setOnClickListener({ onBackPressed() })
    }
}
