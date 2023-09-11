package com.example.chess

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat

class Notepad (val x:Int, val y:Int, val right:Int, val bottom:Int)
{

    fun draw(canvas:Canvas)
    {
        //white background
        //canvas.drawRGB(255, 255, 255);
        //border's properties
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(0F);
        //paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(x.toFloat(), y.toFloat(), right.toFloat(), bottom.toFloat(), paint);
        //canvas.drawRect(0f, 700f, 500f, 800f, paint);
    }

    fun writeText(canvas: Canvas, text:String)
    {
        var paint = Paint()
        paint.setColor(Color.BLUE)
        paint.textSize = 60F
        if (canvas != null) {
            canvas.drawText(text,x.toFloat() , y.toFloat() + paint.textSize, paint)
        }
    }
}