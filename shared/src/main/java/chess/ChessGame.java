package chess;

import java.util.Collection;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor teamTurn = TeamColor.WHITE;
    ChessBoard gameBoard = new ChessBoard();
    ChessPosition whiteKing = new ChessPosition(1, 5);
    ChessPosition blackKing = new ChessPosition(8, 5);

    public ChessGame() {
        gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public TeamColor otherTeam(TeamColor team) {
        if (team == TeamColor.BLACK) {
            return TeamColor.WHITE;
        }
        return TeamColor.BLACK;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (validMoves(move.getStartPosition()).contains(move)){
            ChessPiece pieceMoved = gameBoard.getPiece(move.getStartPosition());
            gameBoard.addPiece(move.getStartPosition(), null);
            if(pieceMoved.getPieceType() == ChessPiece.PieceType.KING) {
                setKingPosition(move.getEndPosition(), teamTurn);
            }
            if(move.getPromotionPiece() != null) {
                gameBoard.addPiece(move.getEndPosition(), new ChessPiece(teamTurn, move.getPromotionPiece()));
            }
            else {
                gameBoard.addPiece(move.getEndPosition(), pieceMoved);
            }
            if (teamTurn == TeamColor.WHITE) {
                setTeamTurn(TeamColor.BLACK);
            }
            else {
                setTeamTurn(TeamColor.WHITE);
            }
        }
        else {
            throw new InvalidMoveException("Invalid Move");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (ChessPosition position: gameBoard.getOccupied(otherTeam(teamColor))) {
            ChessMove takeKing = new ChessMove(position, getKingPosition(teamColor), null);
            ChessMove takeKingPawn = new ChessMove(position, getKingPosition(teamColor), ChessPiece.PieceType.QUEEN);
            if(validMoves(position).contains(takeKing) || validMoves(position).contains(takeKingPawn)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return (isInCheck(teamColor) && validMoves(getKingPosition(teamColor)).isEmpty());
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return (!isInCheck(teamColor) && validMoves(getKingPosition(teamColor)).isEmpty());
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    public void setKingPosition(ChessPosition newPosition, TeamColor team) {
        if (team == TeamColor.WHITE) {
            whiteKing = newPosition;
        }
        else {
            blackKing = newPosition;
        }
    }

    public ChessPosition getKingPosition(TeamColor team) {
        if (team == TeamColor.WHITE) {
            return whiteKing;
        }
        return blackKing;
    }
}
