package edu.uw.eep523.mapslocation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun startLayers(v: View){
        val intent = Intent(this, MapsLayersActivity::class.java).apply {}
        startActivity(intent)
    }



}
