from concurrent import futures
import logging

import grpc
import chess_pb2
import chess_pb2_grpc

class Chess(chess_pb2_grpc.ChessServicer):
    nextMove = ""
    tables = []
    
    def __init__(self):
        for i in range(0, 10):
            self.tables.append( chess_pb2.Table( id = i + 1,blackPlayer="",whitePlayer=""))
            
    def getNextMove(self, request, context):
        print("saved move: %s" % self.nextMove)
        print("request move: %s" % request.move)
        if(len(request.move) > len(self.nextMove)):
            self.nextMove = request.move
        return chess_pb2.Move(move=self.nextMove)
        
    def setNextMove(self, request, context):
        print("set player move: %s" % request.move)
        self.nextMove = request.move
        return chess_pb2.Move(move=self.nextMove)
        
    def setTable(self, request, context):
        freeTables = []
        for table in self.tables:
            if(table.id == request.id):
                table.blackPlayer = request.blackPlayer
                table.whitePlayer = request.whitePlayer
            if len(table.blackPlayer) == 0 or len(table.whitePlayer) == 0:
                freeTables.append(table)
        return chess_pb2.ChessTables(tables=freeTables)    

    def getTables(self, request, context):
        print("Entered GetTables")
        freeTables = []
        for table in self.tables:
            if len(table.blackPlayer) == 0 or len(table.whitePlayer) == 0:
                freeTables.append(table)
        l = len(freeTables)
        print( f'The number is {l}' )
                
        return chess_pb2.ChessTables(tables=freeTables)    
        

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

