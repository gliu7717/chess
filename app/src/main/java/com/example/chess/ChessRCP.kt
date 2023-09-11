package com.example.chess

import android.net.Uri
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.example.chess.ChessGrpcKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable

class ChessRCP(uri: Uri) : Closeable {

    private val channel : ManagedChannel
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

    suspend fun getNextStep(currentMove: String) :String {
        try {
            val request = io.grpc.example.chess.move {
                move = currentMove
            }
            val response = chessRequest.getNextMove(request)
            return response.move
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun close() {
        channel.shutdownNow()
    }
}
