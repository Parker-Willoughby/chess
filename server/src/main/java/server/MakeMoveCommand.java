package server;

import chess.ChessMove;
import commands.UserGameCommand;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, String username, String authToken, Integer gameID, ChessMove move) {
        super(commandType, username, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}