package com.example.chess

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_game)
        val intent = intent
        val isBlack = intent.getBooleanExtra("isBlack",false)
        val tableId = intent.getIntExtra("tableId", 0)
        val blackPlayerName = intent.getStringExtra("blackPlayerName")
        val whitePlayerName = intent.getStringExtra("whitePlayerName")
        setContentView(Game(this, isBlack, tableId, blackPlayerName, whitePlayerName))
    }
}