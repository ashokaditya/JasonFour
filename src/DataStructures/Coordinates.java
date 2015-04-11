package DataStructures;

public class Coordinates {
    private Integer row;
    private Integer col;
    private int hashCode;

    //log(MAX_COL) should be less than offset
    private final static int offset = 7;

    public Coordinates(Integer hashCode) {
        this.hashCode = hashCode;

        row = hashCode >> offset;
        col = hashCode - (row << offset);
    }

    public Integer getRow() {
        return this.row;
    }

    public Integer getCol() {
        return this.col;
    }

    public Coordinates(int row, int col) {
        this.row = Integer.valueOf(row);
        this.col = Integer.valueOf(col);

        this.hashCode = (row << offset) + col;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public static int hashCode(int x, int y){
        return (x << offset) + y;
    }

    public boolean equals(Object obj) {
        Coordinates other = (Coordinates)obj;
        return other == null?false:this.row == other.row && this.col == other.col;
    }

    public int manhattanDistanceTo(Coordinates targetCoordinates) {
        return Math.abs(this.row.intValue() - targetCoordinates.getRow().intValue()) + Math.abs(this.col.intValue() - targetCoordinates.getCol().intValue());
    }

    public static int manhattanDistance(int sourceHash, int targetHash){
        int sourceRow = sourceHash >> offset;
        int targetRow = targetHash >> offset;
        int sourceCol = sourceHash - (sourceRow << offset);
        int targetCol = targetHash - (targetRow << offset);

        return Math.abs(sourceRow - targetRow) + Math.abs(sourceCol - targetCol);
    }
}
