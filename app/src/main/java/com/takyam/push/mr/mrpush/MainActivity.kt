package com.takyam.push.mr.mrpush

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        toolbar?.title = "なろうちぇっくさんです"
        setSupportActionBar(toolbar)

        val button = findViewById(R.id.button) as Button?
        val id = findViewById(R.id.editText) as EditText?
        val pw = findViewById(R.id.editText2) as EditText?
        if(id != null && pw != null) {
            button?.setOnClickListener {
                val idValue = id.text.toString()
                val pwValue = pw.text.toString()
                NarouChecker.updateSession(idValue, pwValue)
                Toast.makeText(applicationContext, "Updating...", 2).show()
            }
        }

        //サービスを起動
        val service : Intent = Intent(applicationContext, MrPushService::class.java)
        startService(service)
    }

}
