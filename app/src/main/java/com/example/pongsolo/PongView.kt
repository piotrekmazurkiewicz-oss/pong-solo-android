package com.example.pongsolo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.random.Random

class PongView(context: Context) : SurfaceView(context), Runnable {
    private var thread: Thread? = null
    private var gameRunning = false
    private var surfaceHolder: SurfaceHolder = holder
    private val paint = Paint()

    // Rozmiary ekranu i obiektów
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var paddleWidth = 0f
    private var paddleHeight = 30f
    private var ballSize = 20f

    // Pozycje
    private var paddleX = 0f
    private var paddleY = 0f
    private var ballX = 0f
    private var ballY = 0f

    // Prędkość piłki
    private var ballDX = 0f
    private var ballDY = 0f

    // Gra
    private var score = 0
    private var highScore = 0  // ← DODAJ
    private var speedIncrease = 1f

    init {
        paint.isAntiAlias = true
        paint.textSize = 120f
        paint.textAlign = Paint.Align.LEFT
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun run() {
        while (gameRunning) {
            if (!holder.surface.isValid) continue

            val canvas = holder.lockCanvas() ?: continue

            update()
            drawElements(canvas)

            holder.unlockCanvasAndPost(canvas)
            try {
                Thread.sleep(16) // 60 FPS
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun update() {
        ballX += ballDX * speedIncrease
        ballY += ballDY * speedIncrease

        // Odbicia od ścian
        if (ballY - ballSize <= 50) ballDY = abs(ballDY)
        if (ballX - ballSize <= 0 || ballX + ballSize >= screenWidth) {
            ballDX = -ballDX
        }

        // Kolizja z paletką
        val paddleRect = RectF(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight)
        val ballRect = RectF(ballX - ballSize, ballY - ballSize, ballX + ballSize, ballY + ballSize)

        if (paddleRect.intersect(ballRect)) {
            ballDY = -abs(ballDY * 1.05f)
            val hitPos = (paddleX + paddleWidth / 2 - ballX) / paddleWidth
            ballDX = hitPos *50f
            score++
            speedIncrease += 0.005f
        }

        if (ballY > screenHeight) {
            resetGame()
        }
    }

    private fun drawElements(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        paint.color = Color.WHITE

        canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint)
        canvas.drawCircle(ballX, ballY, ballSize, paint)

        paint.textSize = 100f
        canvas.drawText("PUNKTY: $score", 50f, 120f, paint)
        canvas.drawText("REKORD: $highScore", 50f, 220f, paint)  // ← DODAJ (2 linie niżej)
        paint.textSize = 40f
        canvas.drawText("Przesuwaj palec", 50f, screenHeight - 50f, paint)
    }

    private fun resetGame() {
        if (score > highScore) highScore = score  // ← DODAJ
        ballX = screenWidth / 2f
        ballY = screenHeight / 2f
        ballDX = (Random.nextFloat() - 0.5f) * 20f  // BYŁO: 400f → 150f
        ballDY = 50f  // BYŁO: -400f → -200f
        speedIncrease = 1f
        score = 0
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()
        paddleWidth = screenWidth / 6f
        paddleX = (screenWidth - paddleWidth) / 2f
        paddleY = screenHeight -380f
        resetGame()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        paddleX = event.x - paddleWidth / 2f
        if (paddleX < 0f) paddleX = 0f
        if (paddleX > screenWidth - paddleWidth) paddleX = screenWidth - paddleWidth
        return true
    }

    fun pause() {
        try {
            gameRunning = false
            while (thread?.isAlive == true) {
                thread?.join()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        gameRunning = true
        thread = Thread(this)
        thread?.start()
    }
}
