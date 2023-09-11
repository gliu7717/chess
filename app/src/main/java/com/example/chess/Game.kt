package com.example.chess

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat

class Game(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var surfaceHolder : SurfaceHolder = holder
    private var gameLoop= GameLoop(this, surfaceHolder)
    private var figureDrawables = arrayOf<Drawable>()
    private var boardDrawable: Drawable
    private var blockWidth = 56
    private var blockHeight = 60
    private var figures:Figures
    var notepad: Notepad


    init{
        holder.addCallback(this)
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        notepad = Notepad(0, height*3/4, width, height)
        var size = (width / 8) * (1f - 56f/504f )

        val boardMap = BitmapFactory.decodeResource(resources, R.drawable.board)
        val boardScaled = Bitmap.createScaledBitmap(boardMap, width, width, true)
        boardDrawable = BitmapDrawable(resources,boardScaled)

        val bMap = BitmapFactory.decodeResource(resources, R.drawable.figures)
        val bMapScaled = Bitmap.createScaledBitmap(bMap, 6*blockWidth, blockHeight * 2, true)

        for(i in 0..1)
            for(j in 0..5){
                var bitmap = Bitmap.createBitmap(bMapScaled, j*blockWidth, i*blockHeight, blockWidth, blockHeight);
                val figureDrawable = BitmapDrawable(resources, bitmap)
                figureDrawables += figureDrawable
        }

        figures = Figures(figureDrawables, size.toInt())
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        gameLoop.startLoop()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    fun drawUPS(canvas: Canvas?){
        var averageUPS : String
        averageUPS =gameLoop.averageUPS.toString()
        var paint = Paint()
        var color = ContextCompat.getColor(context, R.color.magenta)
        paint.color = color
        paint.textSize = 60F
        if (canvas != null) {
            canvas.drawText("UPS: " + averageUPS,100f, 100f,paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.action == MotionEvent.ACTION_DOWN)
            {
                figures.selectFigure(event.x.toInt(), event.y.toInt())
                return true
            }
            else if (event.action == MotionEvent.ACTION_MOVE)
            {
                figures.move(event.x.toInt(), event.y.toInt())
                return true
            }
            else if (event.action == MotionEvent.ACTION_UP){
                figures.confirmMove(event.x.toInt(),event.y.toInt())
                return true
            }
        }
        return super.onTouchEvent(event)
    }


    public fun drawFPS(canvas: Canvas?){
        var averageFPS : String
        averageFPS =gameLoop.averageFPS.toString()
        var paint = Paint()
        var color = ContextCompat.getColor(context, R.color.magenta)
        paint.setColor(color)
        paint.textSize = 60F
        if (canvas != null) {
            canvas.drawText("FPS: " + averageFPS,100f, 200f,paint)
        }
    }
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //drawUPS(canvas)
        //drawFPS(canvas)
        boardDrawable.setBounds(0,0, width, width )
        boardDrawable.draw(canvas)
        figures.draw(canvas)
        notepad.draw(canvas)
        notepad.writeText(canvas, figures.logText)
    }
    fun update()
    {
        figures.update()
    }


}

