package com.bignerdbranch.android.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bignerdbranch.android.criminalintent.databinding.ActivityMainBinding
import java.util.*

private const val TAG = "${CRIMINAL_INTENT_TAG}_MainActivity"
private const val ARG_CRIME_ID = "crime_id"

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}