package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
    Boolean[] kingsRooksMoved = {false, false, false, false, false, false};

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
        ChessPiece piece = gameBoard.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        Collection<ChessMove> validMoves = new ArrayList<>();
        if (piece != null) {
            possibleMoves = piece.pieceMoves(gameBoard, startPosition);
            for (ChessMove move : possibleMoves) {
                if (testMove(move, piece.getTeamColor())) {
                    validMoves.add(move);
                }
            }
            if (piece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(piece.getTeamColor())) {
                validMoves.addAll(getCastleMoves(piece.getTeamColor()));
            }
        }
        return validMoves;
    }

    public Boolean testMove(ChessMove move, TeamColor color) {
        Boolean canMove = false;
        ChessBoard savedBoard = new ChessBoard(gameBoard);
        ChessPosition oldKingPosition = getKingPosition(color);
        makeInvalidMove(move);
        if (!isInCheck(color)) {
            canMove = true;
        }
        setBoard(savedBoard);
        setKingPosition(oldKingPosition, color);
        return canMove;
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
            if (pieceMoved == null || pieceMoved.getTeamColor() != teamTurn) {
                throw new InvalidMoveException("Invalid Move");
            }
            gameBoard.addPiece(move.getStartPosition(), null);
            if(pieceMoved.getPieceType() == ChessPiece.PieceType.KING) {
                setKingPosition(move.getEndPosition(), teamTurn);
                setKingsRooksMoved(move.getStartPosition());
                if (-1 > (move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) || 1 < (move.getStartPosition().getColumn() - move.getEndPosition().getColumn())) {
                    makeRookCastle(move);
                }
            }
            if(pieceMoved.getPieceType() == ChessPiece.PieceType.ROOK) {
                setKingsRooksMoved(move.getStartPosition());
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

    public void makeRookCastle(ChessMove move) {
        ChessMove rightWhite = new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 7), null);
        ChessMove leftWhite = new ChessMove(new ChessPosition(1, 5), new ChessPosition(1, 3), null);
        ChessMove rightBlack = new ChessMove(new ChessPosition(8, 5), new ChessPosition(8, 7), null);
        ChessMove rookMove;
        if (move.equals(rightWhite)) {
            rookMove = new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6), null);
        }
        else if (move.equals(leftWhite)) {
            rookMove = new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4), null);
        }
        else if (move.equals(rightBlack)) {
            rookMove = new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6), null);
        }
        else {
            rookMove = new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4), null);
        }
        ChessPiece pieceMoved = gameBoard.getPiece(rookMove.getStartPosition());
        gameBoard.addPiece(rookMove.getStartPosition(), null);
        gameBoard.addPiece(rookMove.getEndPosition(), pieceMoved);
        setKingsRooksMoved(rookMove.getStartPosition());
    }


    public void makeInvalidMove(ChessMove move) {
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
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessMove takeKing = new ChessMove(position, getKingPosition(teamColor), null);
                ChessMove takeKingPawn = new ChessMove(position, getKingPosition(teamColor), ChessPiece.PieceType.QUEEN);
                ChessPiece piece = gameBoard.getPiece(position);
                if (piece != null) {
                    if (piece.getTeamColor() != teamColor) {
                        if (piece.pieceMoves(gameBoard, position).contains(takeKing) || piece.pieceMoves(gameBoard, position).contains(takeKingPawn)) {
                            return true;
                        }
                    }
                }
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
        if (!isInCheck(teamColor)){
            return false;
        }
        else {
            for (int i = 1; i <= 8; i++) {
                for (int j = 1; j <= 8; j++) {
                    ChessPosition position = new ChessPosition(i, j);
                    if (gameBoard.getPiece(position) != null && gameBoard.getPiece(position).getTeamColor() == teamColor && !validMoves(position).isEmpty()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        Collection<ChessPosition> currentOccupied = new ArrayList<>();
        for (ChessPosition position: gameBoard.getOccupied(teamColor)) {
            currentOccupied.add(new ChessPosition(position.getRow(), position.getColumn()));
        }
        for (ChessPosition position: currentOccupied){
            if (isInCheck(teamColor) || !validMoves(position).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
        ChessPosition startWhiteKing = new ChessPosition(1, 5);
        ChessPosition startBlackKing = new ChessPosition(8, 5);
        for (int i = 1; i <= 8; i ++) {
            for (int j = 1; j <= 8; j ++) {
                ChessPosition position = new ChessPosition(i, j);
                if (board.getPiece(position) != null) {
                    if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == TeamColor.WHITE) {
                        setKingPosition(position, TeamColor.WHITE);
                        if (!position.equals(startWhiteKing)) {
                            kingsRooksMoved[2] = true;
                        }
                    }
                    if (board.getPiece(position).getPieceType() == ChessPiece.PieceType.KING && board.getPiece(position).getTeamColor() == TeamColor.BLACK) {
                        setKingPosition(position, TeamColor.BLACK);
                        if (!position.equals(startBlackKing)) {
                            kingsRooksMoved[5] = true;
                        }
                    }
                }
            }
        }
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

    public Collection<ChessMove> getCastleMoves(TeamColor teamColor) {
        Collection<ChessMove> castleMoves = new ArrayList<>();
        if (teamColor == TeamColor.WHITE && !kingsRooksMoved[2]) {
            ChessPosition rightPosition = new ChessPosition(1, 7);
            ChessPosition leftPosition = new ChessPosition(1, 3);
            ChessMove oneRight = new ChessMove(whiteKing, new ChessPosition(whiteKing.getRow(), whiteKing.getColumn() + 1), null);
            ChessMove oneLeft = new ChessMove(whiteKing, new ChessPosition(whiteKing.getRow(), whiteKing.getColumn() - 1), null);
            ChessMove rightMove = new ChessMove(whiteKing, rightPosition, null);
            ChessMove leftMove = new ChessMove(whiteKing, leftPosition, null);
            if(!kingsRooksMoved[1] && !piecesInbetween(whiteKing, new ChessPosition(1,8)) && testMove(oneRight, TeamColor.WHITE) && testMove(rightMove, TeamColor.WHITE)) {
                castleMoves.add(rightMove);
            }
            if(!kingsRooksMoved[0] && !piecesInbetween(whiteKing, new ChessPosition(1, 1)) && testMove(oneLeft, TeamColor.WHITE) && testMove(leftMove, TeamColor.WHITE)) {
                castleMoves.add(leftMove);
            }
        }
        else if (teamColor == TeamColor.BLACK && !kingsRooksMoved[5]) {
            ChessPosition rightPosition = new ChessPosition(8, 7);
            ChessPosition leftPosition = new ChessPosition(8, 3);
            ChessMove oneRight = new ChessMove(blackKing, new ChessPosition(blackKing.getRow(), blackKing.getColumn() + 1), null);
            ChessMove oneLeft = new ChessMove(blackKing, new ChessPosition(blackKing.getRow(), blackKing.getColumn() - 1), null);
            ChessMove rightMove = new ChessMove(blackKing, rightPosition, null);
            ChessMove leftMove = new ChessMove(blackKing, leftPosition, null);
            if(!kingsRooksMoved[4] && !piecesInbetween(blackKing, new ChessPosition(8, 8)) && testMove(oneRight, TeamColor.BLACK) && testMove(rightMove, TeamColor.BLACK)) {
                castleMoves.add(rightMove);
            }
            if(!kingsRooksMoved[3] && !piecesInbetween(blackKing, new ChessPosition(8, 1)) && testMove(oneLeft, TeamColor.BLACK) && testMove(leftMove, TeamColor.BLACK)) {
                castleMoves.add(leftMove);
            }
        }
        return castleMoves;
    }

    public boolean piecesInbetween(ChessPosition king, ChessPosition rookPosition) {
        int step = 1;
        if (rookPosition.getColumn() - king.getColumn() < 0) {
            step = -1;
        }
        for (int i = king.getColumn() + step; i != rookPosition.getColumn(); i += step) {
            ChessPosition position = new ChessPosition(king.getRow(), i);
            if (gameBoard.getPiece(position) != null) {
                return true;
            }
        }
        return false;
    }

    public void setKingsRooksMoved(ChessPosition startPosition) {
        ChessPosition leftWhiteRook = new ChessPosition(1, 1);
        ChessPosition rightWhiteRook = new ChessPosition(1, 8);
        ChessPosition leftBlackRook = new ChessPosition(8, 1);
        ChessPosition rightBlackRook = new ChessPosition(8, 8);
        ChessPosition startWhiteKing = new ChessPosition(1, 5);
        ChessPosition startBlackKing = new ChessPosition(8, 5);
        ChessPosition[] startingPositions = {leftWhiteRook, rightWhiteRook, startWhiteKing, leftBlackRook, rightBlackRook, startBlackKing};
        for (int i = 0; i <= startingPositions.length - 1; i ++){
            if (startPosition.equals(startingPositions[i])) {
                kingsRooksMoved[i] = true;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(gameBoard, chessGame.gameBoard) && Objects.equals(whiteKing, chessGame.whiteKing) && Objects.equals(blackKing, chessGame.blackKing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, gameBoard, whiteKing, blackKing);
    }
}
