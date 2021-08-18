package com.mahesh_prajapati.mopboxexamples

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mahesh_prajapati.mopboxexamples.location.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBaseActivityValues(true,true)
    }


}