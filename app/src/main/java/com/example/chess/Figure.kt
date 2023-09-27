package com.example.chess

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable

class Figure (var drawable: Drawable, var x:Int, var y:Int, var id:Int, var size:Int)
{
    private var offset = (size * 28f/56f).toInt()
    var position =  Rect(x * size + offset, y*size + offset, (x + 1) *size + offset, (y +1) * size + offset)
    var playerColor = ""    //  W|B
    var chessNote = "" // K|Q|R|B|N|P
    var chessPosition = ""
    var killed = false

    init {
        playerColor = if(y < 2) "B" else "W"
        if(y == 1 || y == 6)
            chessNote = "P"
        else{
            when (x) {
                0, 7 -> chessNote = "R"
                1, 6 -> chessNote = "N"
                2, 5 -> chessNote = "B"
                3 -> chessNote = "Q"
                4 -> chessNote = "K"
            }
        }
        chessPosition += (x + 97).toChar()
        chessPosition += (7 - y + 49).toChar()
    }

    fun draw(canvas: Canvas)
    {
        if(!killed) {
            drawable.setBounds(position)
            drawable.draw(canvas)
        }
    }

    fun setPosition(posX: Int, posY: Int) {
        position.left = posX
        position.top = posY
        position.right = position.left + size
        position.bottom = position.top + size
    }

    fun moveTo(p: Point)
    {
        x = p.x
        y = p.y
        position =  Rect(x * size + offset, y*size + offset, (x + 1) *size + offset, (y +1) * size + offset)
        chessPosition = ""
        chessPosition += (x + 97).toChar()
        chessPosition += (7 - y + 49).toChar()
    }


    fun moveTo(pos:String)
    {
        val p = toCoord(pos[0], pos[1])
        moveTo(p)
    }

    fun update()
    {
    }

    companion object {
        fun toCoord(a:Char, b: Char) :Point
        {
            val p = Point()
            p.x = a.code - 97
            p.y = 7- b.code +49
            return p
        }

    }
}