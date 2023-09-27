package com.example.chess

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect

class ValidMoves(val board:Array<IntArray>, val offset:Int, val size:Int) {
    private var validPositions = arrayListOf<Point>()

    fun isValid(p:Point) :Boolean{
        return validPositions.contains(p)
    }

    fun draw(canvas: Canvas)
    {
        for( p in validPositions){
            var centerX = p.x * size + offset + size/2
            var centerY = p.y * size + offset + size/2

            var position =  Rect(p.x * size + offset, p.y*size + offset, (p.x + 1) *size + offset, (p.y +1) * size + offset)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.GREEN
            canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), size.toFloat()/4, paint)
        }
    }

    fun setValidMoves(figure: Figure, movePositions:String) {
        when (figure.id) {
            6 -> setPawnMoves(figure)
            1 -> setRookMoves(figure)
            2 -> setKnightMoves(figure)
            3 -> setBishopMoves(figure)
            4 -> setQueenMoves(figure)
            5 -> setKingMoves(figure, movePositions)
        }
    }

    private fun setKingMoves(f: Figure, movePositions: String) {
        setBishopMoves(f, 1)
        setRookMoves(f, 1)

        // castling
        if( !movePositions.contains("e1") &&
            !movePositions.contains("a1") &&
            board[7][1] == 0 &&
            board[7][2] == 0 &&
            board[7][3] == 0
        ){
            val p = Point(2,7)
            validPositions += p
        }
        if( !movePositions.contains("e1") &&
            !movePositions.contains("h1") &&
            board[7][5] == 0 &&
            board[7][6] == 0
        ){
            val p = Point(6,7)
            validPositions += p
        }

    }

    private fun setBishopMoves(f: Figure, maxStep:Int = 7) {
        var step = 0
        for( x in f.x + 1..7){
            val dy = x - f.x
            if(dy + f.y > 7)
                break
            if (board [f.y + dy][x] > 0)
                break
            var p = Point(x, f.y + dy)
            validPositions += p
            if(++step >= maxStep)
                break
            if (board [f.y + dy][x] < 0)
                break
        }
        step = 0
        for( x in f.x + 1..7){
            val dy = x - f.x
            if(f.y - dy <0)
                break
            if (board [f.y - dy][x] > 0)
                break
            var p = Point(x, f.y - dy)
            validPositions += p
            if(++step >= maxStep)
                break
            if (board [f.y - dy][x] < 0)
                break
        }
        step = 0
        for( x in f.x - 1 downTo 0){
            val dy = f.x - x
            if(dy + f.y >7)
                break
            if (board [f.y + dy][x] > 0)
                break
            var p = Point(x, f.y + dy)
            validPositions += p
            if(++step >= maxStep)
                break
            if (board [f.y + dy][x] < 0)
                break
        }
        step = 0
        for( x in f.x - 1 downTo 0){
            val dy = f.x - x
            if(f.y - dy  < 0)
                break
            if (board [f.y - dy][x] > 0)
                break
            var p = Point(x, f.y - dy)
            validPositions += p
            if(++step >= maxStep)
                break
            if (board [f.y - dy][x] < 0)
                break
        }
    }

    private fun setKnightMoves(f: Figure) {
        var p = Point(f.x -2, f.y)
        if(p.x>= 0){
            p.y = f.y+1
            if(p.y <8 && board [p.y][p.x] <= 0)
                validPositions += p
            p.y = f.y-1
            if(p.y >= 0 && board [p.y][p.x] <= 0)
                validPositions += p
        }
        var p1 = Point(f.x +2, f.y)
        if(p1.x <8){
            p1.y = f.y+1
            if(p1.y <8 && board [p1.y][p1.x] <= 0)
                validPositions += p1
            p1.y = f.y-1
            if(p1.y >= 0 && board [p1.y][p1.x] <= 0)
                validPositions += p1
        }
        var p2 = Point(f.x -1, f.y)
        if(p2.x>= 0){
            p2.y = f.y+2
            if(p2.y <8 && board [p2.y][p2.x] <= 0)
                validPositions += p2
            p2.y = f.y-2
            if(p2.y >= 0 && board [p2.y][p2.x] <= 0)
                validPositions += p2
        }

        var p3 = Point(f.x +1, f.y)
        if(p3.x <8){
            p3.y = f.y+2
            if(p3.y <8 && board [p3.y][p3.x] <= 0)
                validPositions += p3
            p3.y = f.y-2
            if(p3.y >= 0 && board [p3.y][p3.x] <= 0)
                validPositions += p3
        }
    }

    private fun setRookMoves(f: Figure, maxStep:Int = 7) {
        var step = 0
        for( x in f.x + 1..7){
            if(board [f.y][x] > 0)
                break
            var p = Point(x, f.y)
                validPositions += p
            if(++step >= maxStep)
                break
            if(board [f.y][x] < 0)
                break
        }
        step = 0
        for( x in f.x - 1 downTo 0){
            if(board [f.y][x] > 0)
                break
            var p = Point(x, f.y)
            validPositions += p
            if(++step >= maxStep)
                break
            if(board [f.y][x] < 0)
                break
        }
        step = 0
        for( y in f.y + 1..7){
            if(board [y][f.x] > 0)
                break
            var p = Point(f.x, y)
            validPositions += p
            if(++step >= maxStep)
                break
            if(board [y][f.x] < 0)
                break
        }
        step = 0
        for( y in f.y - 1 downTo 0){
            if(board [y][f.x] > 0)
                break
            var p = Point(f.x, y)
            validPositions += p
            if(++step >= maxStep)
                break
            if(board [y][f.x] < 0)
                break
        }
        // castling
    }

    private fun setQueenMoves(f: Figure) {
        setRookMoves(f)
        setBishopMoves(f)
    }

    private fun setPawnMoves(figure: Figure) {
        if (figure.y == 0)
        {
            figure.id = 3
            setQueenMoves(figure)
            return
        }
        var dy = -1
        val p = Point(figure.x, figure.y + dy)
        if(board [p.y][p.x] == 0){
            validPositions += p
        }
        if(figure.y == 6){
            dy = -2
            val p = Point(figure.x, figure.y + dy)
            if(board [p.y][p.x] == 0)
                validPositions += p
        }
        if(figure.x >0  && board [p.y][figure.x -1] < 0){
            val p1 = Point(figure.x - 1, p.y)
            validPositions += p1
        }
        if(figure.x < 7  && board [p.y][figure.x + 1] < 0){
            val p1 = Point(figure.x + 1, p.y)
            validPositions += p1
        }

        //En passant
    }

    fun clear() {
        validPositions.clear()
    }
}