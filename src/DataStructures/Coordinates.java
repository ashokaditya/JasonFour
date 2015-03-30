package DataStructures;

public class Coordinates {
    private Integer x;
    private Integer y;
    private int hashCode;

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Coordinates(int x, int y){
        this.x = x;
        this.y = y;

        final int prime = 57;

        hashCode = (prime + this.x.hashCode()) * prime + this.y.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        Coordinates other = (Coordinates) obj;

        return this.x.equals(other.x) && this.y.equals(other.y);
    }

    public int manhattanDistanceTo(Coordinates targetCoordinates) {
        return Math.abs(x - targetCoordinates.getX()) + Math.abs(y - targetCoordinates.getY());
    }
}
