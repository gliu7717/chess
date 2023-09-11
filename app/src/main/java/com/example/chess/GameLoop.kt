package com.example.chess

import android.view.SurfaceHolder

class GameLoop(var game: Game, var surfaceHolder: SurfaceHolder): Thread() {
    var averageFPS: Double = 0.0
    var averageUPS: Double = 0.0

    var MAX_UPS = 30.0
    var UPS_PERIOD = 1E3/MAX_UPS
    var isRunning = false

    fun startLoop(){
        isRunning = true
        start()
    }

    override fun run(){
        super.run()
        var updateCount = 0
        var frameCount = 0

        var startTime = System.currentTimeMillis()
        var elapsedTime = 0L
        var sleepTime = 0.0
        while(isRunning)
        {
            var canvas = surfaceHolder.lockCanvas()
            synchronized(surfaceHolder)
            {
                game.update()
                updateCount ++
                if(canvas!=null)
                    game.draw(canvas)
            }
            try {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }catch (e:Exception)
            {

            }
            frameCount ++
            // pause game to  not exceed UPS
            sleepTime = (updateCount * UPS_PERIOD - elapsedTime)
            if(sleepTime> 0.0)
            {
                sleep(sleepTime.toLong())
            }

            sleepTime = (updateCount * UPS_PERIOD - elapsedTime)
            if(sleepTime> 0.0)
            {
                sleep(sleepTime.toLong())
            }
            // skip frames to keep up with target UPS
            while (sleepTime < 0.0 && updateCount < MAX_UPS -1 )
            {
                game.update()
                updateCount++
                elapsedTime = System.currentTimeMillis() - startTime
                sleepTime = (updateCount * UPS_PERIOD - elapsedTime)

            }
            // calcuate average UPS FPS
            elapsedTime = System.currentTimeMillis() - startTime
            if(elapsedTime >= 1000L)
            {
                averageUPS = updateCount / (1E-3 * elapsedTime)
                averageFPS = frameCount / (1E-3 * elapsedTime)
                updateCount = 0
                frameCount = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}