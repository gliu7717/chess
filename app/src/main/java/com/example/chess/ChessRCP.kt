package com.example.chess

import android.net.Uri
import android.util.Log
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.example.chess.ChessGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import java.io.Closeable
import io.grpc.example.chess.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ChessRCP(uri: Uri) : Closeable {

    private val channel : ManagedChannel
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    init {
        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }
        channel =  builder.executor(Dispatchers.IO.asExecutor()).build()
    }
    /*
    private val channel = let {
        val builder = ManagedChannelBuilder.forAddress(uri.host, uri.port)
        if (uri.scheme == "https") {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }
        builder.executor(Dispatchers.IO.asExecutor()).build()
    }
     */

    private val chessRequest = ChessGrpcKt.ChessCoroutineStub(channel)

    suspend fun getNextStep(tableId: Int, currentMove: String) :String {
        try {
            val request = io.grpc.example.chess.table {
                id= tableId
                blackPlayer = ""
                whitePlayer = ""
                move = currentMove
            }
            val response = chessRequest.getNextMove(request)
            return response.move
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    suspend fun getTables() :List<Table>? {
        try {
            val request = io.grpc.example.chess.noparam {
            }
            val response = chessRequest.getTables(request)
            return response.tablesList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun setTable(table: Table) :List<Table>? {
        try {
            val request = table
            val response = chessRequest.setTable(request)
            return response.tablesList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun close() {
        channel.shutdownNow()
    }


    companion object {
        private lateinit var INSTANCE: ChessRCP
        @JvmStatic
        fun getInstance(uri: Uri): ChessRCP {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = ChessRCP(uri)
            }
            return INSTANCE
        }
        fun getInstance(): ChessRCP {
            return INSTANCE
        }
    }

}
