from concurrent import futures
import logging

import grpc
import chess_pb2
import chess_pb2_grpc

class Chess(chess_pb2_grpc.ChessServicer):
    nextMove = ""
    def getNextMove(self, request, context):
        print("player move: %s" % request.move)
        print("computer move: %s" % self.nextMove)
        return chess_pb2.Move(move=self.nextMove)    
    def setNextMove(self, request, context):
        print("set player move: %s" % request.move)
        self.nextMove = request.move
        return chess_pb2.Move(move=self.nextMove)


def serve():
    port = "50051"
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    chess_pb2_grpc.add_ChessServicer_to_server(Chess(), server)
    server.add_insecure_port("[::]:" + port)
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()


if __name__ == "__main__":
    logging.basicConfig()
    serve()

