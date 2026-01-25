package com.example.pongsolo

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    private lateinit var gameView: PongView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = PongView(this)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.pause()
    }
}
