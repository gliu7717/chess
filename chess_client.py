from __future__ import print_function

import logging

import grpc
import chess_pb2
import chess_pb2_grpc
import sys


def run(move):
    with grpc.insecure_channel("localhost:50051") as channel:
        stub = chess_pb2_grpc.ChessStub(channel)
        response = stub.setNextMove(chess_pb2.Move(move=move))
        response = stub.getNextMove(chess_pb2.Move(move="e2e4"))
    print("chess client received: " + response.move)


if __name__ == "__main__":
    logging.basicConfig()
    move = ""
    result = f'Number of arguments: {len(sys.argv)} arguments.'
    print(result)
    result = f'Argument List: {sys.argv}'
    if(len(sys.argv) >= 2):
        move  = sys.argv[1]
    print(result)
    run(move)

