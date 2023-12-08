package com.example.chess

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.checkerframework.checker.units.qual.h
import java.lang.Thread.sleep
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class Figures (var figureDrawables: Array<Drawable>, var size:Int, val boardOffset:Int, val isBlack:Boolean, val tableId:Int) {
    private var offset = (size * 28f / 56f).toInt()
    private var figures = arrayOf<Figure>()
    private var board = arrayOf(
        intArrayOf(-1, -2, -3, -4, -5, -3, -2, -1),
        intArrayOf(-6, -6, -6, -6, -6, -6, -6, -6),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6),
        intArrayOf(1, 2, 3, 4, 5, 3, 2, 1),
    )
    private var isMoving = false
    private var playerMove = !isBlack
    private var selectedFigure: Figure? = null
    private var newDestPosition: Point? = null
    private var movePositions = ""
    var logText = ""
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var startedOpponentQuery = false


    //private val uri by lazy { Uri.parse("http://150.136.175.102:50051/") }
    //private val uri by lazy { Uri.parse("http://192.168.0.129:50051/") }
    //private val uri by lazy { Uri.parse("http://10.0.0.9:50051/") }
    //private val chessRCP by lazy { ChessRCP(uri) }
    private var validMoves = ValidMoves(board, offset, size, boardOffset)

    init {
        load()
    }

    fun load() {
        for (i in 0..7)
            for (j in 0..7) {
                val n = board[i][j]
                if (n == 0)
                    continue
                val x = kotlin.math.abs(n) - 1
                var y = 0
                if (n > 0) y = 1
                var index = y * 6 + x
                figures += Figure(figureDrawables[index], j, i, n, size, boardOffset)
            }
    }

    fun toChessNote(p:Point):String {
        var chessPosition = ""
        chessPosition += (p.x + 97).toChar()
        chessPosition += (7 - p.y + 49).toChar()
        return chessPosition
    }

    fun draw(canvas: Canvas)
    {
        for(f in figures)
            f.draw(canvas)
        validMoves.draw(canvas)
    }

    fun contain(x:Int, y:Int):Figure?
    {
        for(f in figures){
            if(f.position.contains(x,y))
                return f
        }
        return null
    }

    fun update()
    {
        if(isMoving)
            return
        myMove()
        computerMove()

        for(f in figures)
            f.update()

    }

    private fun myMove() {
        if( playerMove && selectedFigure!= null ){
            if(newDestPosition!=null) {
                Log.i("MYTAG", "new Position: ${newDestPosition!!.x} ${newDestPosition!!.y} ")
                var oldPosition =selectedFigure!!.chessPosition
                if(validMoves.isValid(newDestPosition!!) ) {
                    val p1 = Figure.toCoord(oldPosition[0], oldPosition[1])
                    board[newDestPosition!!.y][newDestPosition!!.x] = selectedFigure!!.id
                    board[p1.y][p1.x] = 0
                    selectedFigure!!.moveTo(newDestPosition!!);
                    playerMove = false
                    if(movePositions.isEmpty())
                        movePositions = oldPosition + selectedFigure!!.chessPosition
                    else
                        movePositions += " " + oldPosition + selectedFigure!!.chessPosition
                    logText = movePositions
                    validMoves.clear()

                    //castling move
                    myCastlingMove()
                    newDestPosition = null
                    return
                }
            }
            selectedFigure!!.moveTo(selectedFigure!!.chessPosition);
            newDestPosition= null
        }
        validMoves.clear()
    }

    private fun myCastlingMove()
    {
        if(selectedFigure!!.id == 5 && // king
            newDestPosition!!.x == 6 &&
            newDestPosition!!.y == 7) {
            val myRook = figures[31]
            val p = Point(5,7)
            board[7][5] = myRook.id
            board[7][7] = 0
            myRook.moveTo(p)
        }
        if( selectedFigure!!.id == 5 && // king
            newDestPosition!!.x == 2 &&
            newDestPosition!!.y == 7) {
            val myRook = figures[24]
            val p = Point(3,7)
            board[7][3] = myRook.id
            board[7][0] = 0
            myRook.moveTo(p)
        }
    }

    private fun computerMove() {
        if(playerMove)  // computer move
            return
        lock.withLock{
            condition.signal()
        }
        if(startedOpponentQuery)
            return
        startedOpponentQuery = true

        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                lock.withLock{
                    condition.await()
                }

                while (!getOpponentMove()){
                    delay(500)
                }
            }
        }
    }

    suspend fun getOpponentMove():Boolean
    {
        val newMoveString = ChessRCP.getInstance().getNextStep(tableId, movePositions)
        if(newMoveString.length > movePositions.length)
        {
            movePositions = newMoveString
            val destPosition = movePositions.substring(movePositions.length-4, movePositions.length )
            val convertedPosition = reverseChessPosition(destPosition)
            newDestPosition = null
            move(convertedPosition)
            playerMove = true
            Log.i("MYTAG","onTouchEvent: moves:" + movePositions)
            logText = movePositions
            newDestPosition = null
            return true
        }
        return false
    }

    private fun reverseChessPosition(pos: String): String {
        if(pos.length < 4)
            return ""
        var p1 = Figure.toCoord(pos[0], pos[1])
        p1.x = 7-p1.x
        p1.y = 7-p1.y
        var p2 = Figure.toCoord(pos[2], pos[3])
        p2.x = 7-p2.x
        p2.y = 7-p2.y
        return Figure.toChessPosition(p1) +Figure.toChessPosition(p2)
    }

    fun findChessPosition(x: Int, y: Int):Point? {
         val p = Point()
         p.x = (x - offset) /size
         p.y = (y - offset  - boardOffset) / size
         if(p.x < 0 || p.x > 7  || p.y < 0 || p.y > 7)
            return null
         return p
    }

    fun move(value: String) {
        Log.i("MYTAG", "value : $value")
        if(value.length < 4)
            return
        val oldPos = value.substring(0,2)
        val newPos = value.substring(2,4)
        val p1 = Figure.toCoord(oldPos[0], oldPos[1])
        val p2 = Figure.toCoord(newPos[0], newPos[1])
        for(f in figures) {
            if(f.chessPosition == newPos){
                f.killed = true
            }
            else if(f.chessPosition == oldPos){
                board[p2.y][p2.x] = f.id
                board[p1.y][p1.x] = 0
                f.moveTo(newPos)
            }
        }
    }

    fun selectFigure(x: Int, y: Int) {
        if(!playerMove)
            return

        var figure = contain(x, y)
        if(figure==null || figure.id < 0)
            return
        isMoving = true
        selectedFigure = figure
        Log.i("MYTAG", "onTouchEvent: ${selectedFigure!!.x}:${selectedFigure!!.y} selected")
        logText = "${selectedFigure!!.x}:${selectedFigure!!.y}"
        Log.i("MYTAG", "onTouchEvent: ${selectedFigure!!.position} ")
        Log.i("MYTAG", "onTouchEvent: playerColor: ${figure.playerColor} chessPosition: ${figure.chessPosition} chessNote: ${figure.chessNote} ")
        logText += " ${figure.playerColor}  ${figure.chessPosition}  ${figure.chessNote}"
        figure.setPosition(x, y);
        Log.i("MYTAG", "new Position: ${figure.position} ")
        validMoves.setValidMoves(figure, movePositions)
    }


    fun move(x:Int, y:Int)
    {
        if(!playerMove)
            return
        if(isMoving && selectedFigure!= null)
            selectedFigure!!.setPosition(x, y);
    }

    fun confirmMove(x:Int, y:Int)
    {
        if(!playerMove)
            return
        isMoving = false
        if(selectedFigure != null)
            newDestPosition = findChessPosition(x,y )
     }

}