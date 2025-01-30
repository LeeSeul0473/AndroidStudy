package com.example.activitystudy

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FragmentStudy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fragment_study)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        settingButtons()
    }

    fun settingButtons(){
        val button_mia = findViewById<Button>(R.id.button_mia_fragment)
        val button_myeongho = findViewById<Button>(R.id.button_myeongho_fragment)

        button_mia.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, MiaFragment())
            fragmentTransaction.commit()
        }

        button_myeongho.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, MyeonghoFragment())
            fragmentTransaction.commit()
        }
    }
}