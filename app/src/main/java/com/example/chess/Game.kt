package com.example.chess

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView


class Game(context: Context, val isBlack:Boolean = false,
           val tableId:Int,
           var blackPlayerName:String?,
           var whitePlayerName:String?
    ) : SurfaceView(context), SurfaceHolder.Callback {
    private var surfaceHolder : SurfaceHolder = holder
    private var gameLoop= GameLoop(this, surfaceHolder)
    private var figureDrawables = arrayOf<Drawable>()
    private var boardDrawable: Drawable
    private var blockWidth = 56
    private var blockHeight = 60
    private var offset = 120
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
                var h = i
                if(isBlack)
                    h = 1 - i
                var bitmap = Bitmap.createBitmap(bMapScaled, j*blockWidth, h*blockHeight, blockWidth, blockHeight);
                val figureDrawable = BitmapDrawable(resources, bitmap)
                figureDrawables += figureDrawable
        }
        figures = Figures(figureDrawables, size.toInt(), offset, isBlack, tableId)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        gameLoop.startLoop()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    @SuppressLint("ResourceAsColor")
    fun drawUPS(canvas: Canvas?){
        var averageUPS : String
        averageUPS =gameLoop.averageUPS.toString()
        var paint = Paint()
        paint.color = R.color.magenta
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


    @SuppressLint("ResourceAsColor")
    public fun drawFPS(canvas: Canvas?){
        var averageFPS : String
        averageFPS =gameLoop.averageFPS.toString()
        var paint = Paint()
        paint.setColor(R.color.magenta)
        paint.textSize = 60F
        if (canvas != null) {
            canvas.drawText("FPS: " + averageFPS,100f, 200f,paint)
        }
    }
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        //drawUPS(canvas)
        //drawFPS(canvas)
        boardDrawable.setBounds(0,offset, width, width + offset )
        boardDrawable.draw(canvas)
        figures.draw(canvas)
        notepad.draw(canvas)
        notepad.writeText(canvas, figures.logText)
        drawPlayerName(canvas)
    }

    private fun drawPlayerName(canvas: Canvas) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(0F);
        canvas.drawRect(0f, 0f, width.toFloat(), offset.toFloat(), paint);
        val h = (offset+width).toFloat()
        canvas.drawRect(0f, h, width.toFloat(), h + offset, paint);

        paint.textSize = 60F
        whitePlayerName = "Ben"
        blackPlayerName = "Ray Liu"
        val whiteBounds = Rect()
        paint.getTextBounds(whitePlayerName, 0, whitePlayerName!!.length, whiteBounds)
        val blackBounds = Rect()
        paint.getTextBounds(blackPlayerName, 0, blackPlayerName!!.length, blackBounds)

        if(isBlack) {
            paint.setColor(Color.YELLOW)
            var x = width/2 - (blackBounds.right - blackBounds.left)/2f
            var yoffset = offset/2 - (whiteBounds.bottom - whiteBounds.top)/2f
            canvas.drawText(blackPlayerName!!, x, whiteBounds.bottom - whiteBounds.top + width + offset + yoffset, paint)
            x = width/2 - (whiteBounds.right - whiteBounds.left)/2f
            paint.setColor(Color.WHITE)
            canvas.drawText(whitePlayerName!!, x, whiteBounds.bottom - whiteBounds.top + yoffset, paint)
        }
        else{
            paint.setColor(Color.YELLOW)
            var x = width/2 - (whiteBounds.right - whiteBounds.left)/2f
            var yoffset = offset/2 - (whiteBounds.bottom - whiteBounds.top)/2f
            canvas.drawText(whitePlayerName!!, x, whiteBounds.bottom - whiteBounds.top + width + offset + yoffset, paint)
            x = width/2 - (blackBounds.right - blackBounds.left)/2f
            paint.setColor(Color.WHITE)
            canvas.drawText(blackPlayerName!!, x, blackBounds.bottom - blackBounds.top + yoffset, paint)
        }

    }

    fun update()
    {
        figures.update()
    }


}

