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

        }
        else if (piece.getPieceType() == PieceType.BISHOP) {
            return List.of(new ChessMove(new ChessPosition(5,4), new ChessPosition(1, 8), null));
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
        }
        else if (piece.getPieceType() == PieceType.PAWN) {
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
