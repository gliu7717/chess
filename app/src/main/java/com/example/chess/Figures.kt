package com.example.chess

import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class Figures (var figureDrawables: Array<Drawable>, var size:Int) {

    private var offset = (size * 28f / 56f).toInt()
    private var figures = arrayOf<Figure>()
    private val board = arrayOf(
        intArrayOf(-1, -2, -3, -4, -5, -3, -2, -1),
        intArrayOf(-6, -6, -6, -6, -6, -6, -6, -6),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(6, 6, 6, 6, 6, 6, 6, 6),
        intArrayOf(1, 2, 3, 4, 5, 3, 2, 1),
    )
    private var isMove = false
    private var playerMove = true
    private var selectedFigure: Figure? = null
    private var newDestPosition: Point? = null
    private var movePositions = ""
    var logText = ""

    private val uri by lazy { Uri.parse("http://150.136.175.102:50051/") }
    private val chessRCP by lazy { ChessRCP(uri) }


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
                figures += Figure(figureDrawables[index], j, i, size)
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
        if(isMove)
            return
        myMove()
        computerMove()

        for(f in figures)
            f.update()

    }

    private fun myMove() {
        if(!playerMove)  // computer move
            return
        if( selectedFigure!= null ){
            if(newDestPosition!=null) {
                Log.i("MYTAG", "new Position: ${newDestPosition!!.x} ${newDestPosition!!.y} ")
                var oldPosition =selectedFigure!!.chessPosition

                if(oldPosition == toChessNote(newDestPosition!!)) {
                    newDestPosition = null
                    return
                }
                selectedFigure!!.moveTo(newDestPosition!!);
                playerMove = false
                newDestPosition = null
                movePositions += oldPosition + selectedFigure!!.chessPosition + " "
                logText = movePositions
            }
            else {
                selectedFigure!!.moveTo(selectedFigure!!.chessPosition);
                newDestPosition= null
            }
        }
    }

    private fun computerMove() {
        if(playerMove)  // computer move
            return
        playerMove = true
        CoroutineScope(Dispatchers.IO).launch {
            val responseMsg = chessRCP.getNextStep(movePositions)
            Log.i("MYTAG", "New pos: " + responseMsg)
            /*
            movePositions += chessRCP.responseState.value + " "
            newDestPosition = null
            move(chessRCP.responseState.value)
             */
            if(responseMsg.isNotEmpty()) {
                movePositions += responseMsg + " "
                newDestPosition = null
                move(responseMsg)
                Log.i("MYTAG","onTouchEvent: moves:" + movePositions)
                logText = movePositions
                newDestPosition = null
            }
        }
    }

    fun findChessPosition(x: Int, y: Int):Point? {
         val p = Point()
         p.x = (x - offset) /size
         p.y = (y - offset) / size
         if(p.x < 0 || p.x > 7  || p.y < 0 || p.y > 7)
            return null
         return p
    }


    fun move(value: String) {
        val oldPos = value.substring(0,2)
        val newPos = value.substring(2,4)
        for(f in figures) {
            if(f.chessPosition == newPos){
                f.killed = true
            }
            else if(f.chessPosition == oldPos){
                f.moveTo(newPos)
            }
        }
    }

    fun selectFigure(x: Int, y: Int) {
        if(!playerMove)
            return

        var figure = contain(x, y)
        if(figure==null)
            return
        isMove = true
        selectedFigure = figure
        Log.i("MYTAG", "onTouchEvent: ${selectedFigure!!.x}:${selectedFigure!!.y} selected")
        logText = "${selectedFigure!!.x}:${selectedFigure!!.y}"
        Log.i("MYTAG", "onTouchEvent: ${selectedFigure!!.position} ")
        Log.i("MYTAG", "onTouchEvent: playerColor: ${figure.playerColor} chessPosition: ${figure.chessPosition} chessNote: ${figure.chessNote} ")
        logText += " ${figure.playerColor}  ${figure.chessPosition}  ${figure.chessNote}"
        figure.setPosition(x, y);
        Log.i("MYTAG", "new Position: ${figure.position} ")
    }

    fun move(x:Int, y:Int)
    {
        if(isMove && selectedFigure!= null)
            selectedFigure!!.setPosition(x, y);
    }

    fun confirmMove(x:Int, y:Int)
    {
        isMove = false
        if(selectedFigure == null)
            return
        newDestPosition = findChessPosition(x,y )
     }

}