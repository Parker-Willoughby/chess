package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public boolean IsOffBoard(ChessPosition position) {
        return (1 > position.getRow() || position.getRow() > 8 || 1 > position.getColumn() || position.getColumn() > 8 );
    }

    public String RanIntoPiece(ChessPosition position, ChessBoard board) {
        if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() == pieceColor) {
            return "On Team";
        }
        else if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() != pieceColor) {
            return "Other Team";
        }
        else {
            return "Empty";
        }
    }

    public Collection<ChessMove> expandVectors(ChessPosition startPosition, int[][] baseVectors, ChessBoard board) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        for(int[] direction: baseVectors) {
            ChessPosition nextPosition = new ChessPosition(startPosition.getRow() + direction[0], startPosition.getColumn() + direction[1]);
            while (!IsOffBoard(nextPosition) && RanIntoPiece(nextPosition, board) != "On Team") {
                validMoves.add(new ChessMove(startPosition, nextPosition, null));
                if(RanIntoPiece(nextPosition, board) == "Other Team") {
                    break;
                }
                nextPosition = new ChessPosition(nextPosition.getRow() + direction[0], nextPosition.getColumn() + direction[1]);
            }
        }
        return validMoves;
    }


    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        if (piece.getPieceType() == PieceType.KING) {
            List<ChessPosition> kingPositions = List.of(new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()),
                    new ChessPosition(myPosition.getRow(), myPosition.getColumn() + 1),
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()),
                    new ChessPosition(myPosition.getRow(), myPosition.getColumn() - 1),
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1),
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1),
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1),
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1));
            for (ChessPosition position:kingPositions) {
                if (!IsOffBoard(position) && RanIntoPiece(position, board) != "On Team") {
                    validMoves.add(new ChessMove(myPosition, position, null));
                }
            }
        }
        else if (piece.getPieceType() == PieceType.QUEEN) {
            int [][] queenVectors = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            validMoves = expandVectors(myPosition, queenVectors, board);
        }
        else if (piece.getPieceType() == PieceType.BISHOP) {
            int [][] bishopVectors = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
            validMoves = expandVectors(myPosition, bishopVectors, board);
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {
            List<ChessPosition> knightPositions = List.of(new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1),
                    new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1),
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2),
                    new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2),
                    new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1),
                    new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1),
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2),
                    new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2));
            for (ChessPosition position:knightPositions) {
                if (!IsOffBoard(position) && RanIntoPiece(position, board) != "On Team") {
                    validMoves.add(new ChessMove(myPosition, position, null));
                }
            }
        }
        else if (piece.getPieceType() == PieceType.ROOK) {
            int [][] rookVectors = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            validMoves = expandVectors(myPosition, rookVectors, board);
        }
        else if (piece.getPieceType() == PieceType.PAWN) {
            int[] moveNumbers = new int[3];
            if (pieceColor == ChessGame.TeamColor.WHITE) {
                moveNumbers[0] = 1;
                moveNumbers[1] = 8;
                moveNumbers[2] = 2;
            }
            else {
                moveNumbers[0] = -1;
                moveNumbers[1] = 1;
                moveNumbers[2] = 7;
            }
            ChessPosition forwardPosition = new ChessPosition(myPosition.getRow() + moveNumbers[0], myPosition.getColumn());
            ChessPosition diagonalRight = new ChessPosition(myPosition.getRow() + moveNumbers[0], myPosition.getColumn() + 1);
            ChessPosition diagonalLeft = new ChessPosition(myPosition.getRow() + moveNumbers[0], myPosition.getColumn() - 1);
            ChessPosition doubleForward = new ChessPosition(myPosition.getRow() + 2*moveNumbers[0], myPosition.getColumn());
            if(RanIntoPiece(forwardPosition, board) == "Empty" && !IsOffBoard(forwardPosition)) {
                if(forwardPosition.getRow() == moveNumbers[1]) {
                    validMoves.add(new ChessMove(myPosition, forwardPosition, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, forwardPosition, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, forwardPosition, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, forwardPosition, PieceType.BISHOP));
                }
                else {
                    validMoves.add(new ChessMove(myPosition, forwardPosition, null));
                    if (myPosition.getRow() == moveNumbers[2] && RanIntoPiece(doubleForward, board) == "Empty") {
                        validMoves.add(new ChessMove(myPosition, doubleForward, null));
                    }
                }
            }
            if(!IsOffBoard(diagonalRight) && RanIntoPiece(diagonalRight, board) == "Other Team") {
                if(diagonalRight.getRow() == moveNumbers[1]) {
                    validMoves.add(new ChessMove(myPosition, diagonalRight, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, diagonalRight, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, diagonalRight, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, diagonalRight, PieceType.BISHOP));
                }
                else {
                    validMoves.add(new ChessMove(myPosition, diagonalRight, null));
                }
            }
            if(!IsOffBoard(diagonalLeft) && RanIntoPiece(diagonalLeft, board) == "Other Team") {
                if(diagonalLeft.getRow() == moveNumbers[1]) {
                    validMoves.add(new ChessMove(myPosition, diagonalLeft, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, diagonalLeft, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, diagonalLeft, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, diagonalLeft, PieceType.BISHOP));
                }
                else {
                    validMoves.add(new ChessMove(myPosition, diagonalLeft, null));
                }
            }
        }
        return validMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
