package service.records;

import chess.ChessGame;

public record GameCreate(String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
