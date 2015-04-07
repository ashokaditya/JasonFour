package DataStructures;

public class Coordinates {
    private Integer x;
    private Integer y;
    private int hashCode;

    //log(MAX_COL) should be less than offset
    private final static int offset = 7;

    public Coordinates(Integer hashCode) {
        this.hashCode = hashCode;

        x = hashCode >> offset;
        y = hashCode - (x << offset);
    }

    public Integer getX() {
        return this.x;
    }

    public Integer getY() {
        return this.y;
    }

    public Coordinates(int x, int y) {
        this.x = Integer.valueOf(x);
        this.y = Integer.valueOf(y);

        this.hashCode = (x << offset) + y;
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
        return other == null?false:this.x == other.x && this.y == other.y;
    }

    public int manhattanDistanceTo(Coordinates targetCoordinates) {
        return Math.abs(this.x.intValue() - targetCoordinates.getX().intValue()) + Math.abs(this.y.intValue() - targetCoordinates.getY().intValue());
    }
}
