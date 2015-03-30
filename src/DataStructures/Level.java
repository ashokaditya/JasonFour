package DataStructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class Level {
    private static HashSet<Coordinates> walls = new HashSet<Coordinates>();
    private static HashMap<Coordinates, Character> goalsByCoordinates = new HashMap<Coordinates, Character>();
    private static HashMap<Character, HashSet<Coordinates>> goalsByCharacter = new HashMap<Character, HashSet<Coordinates>>();

    private Level() {
    }

    public static void addWall(int x, int y) {
        walls.add(new Coordinates(x, y));
    }

    public static void addGoal(int x, int y, char goalLetter) {
        Coordinates coordinates = new Coordinates(x, y);

        goalsByCoordinates.put(coordinates, goalLetter);

        if(goalsByCharacter.containsKey(goalLetter)){
            goalsByCharacter.get(goalLetter).add(coordinates);
        }
        else{
            HashSet<Coordinates> newCoordinates = new HashSet<Coordinates>();
            newCoordinates.add(coordinates);
            goalsByCharacter.put(goalLetter, newCoordinates);
        }
    }

    public static char getGoal(int x, int y) {
        return goalsByCoordinates.get(new Coordinates(x, y));
    }

    public static boolean hasGoal(int x, int y) {
        return goalsByCoordinates.containsKey(new Coordinates(x, y));
    }

    public static boolean hasWall(int row, int col) {
        return walls.contains(new Coordinates(row, col));
    }

    public static Set<Character> getGoals() {
        return goalsByCharacter.keySet();
    }

    public static HashSet<Coordinates> getGoalCoordinates(char goalLetter) {
        return goalsByCharacter.get(Character.toLowerCase(goalLetter));
    }
}
