package DataStructures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class Level {
    private static HashSet<Integer> walls = new HashSet<Integer>();
    private static HashMap<Integer, Character> goalsByCoordinates = new HashMap<Integer, Character>();
    private static HashMap<Character, HashSet<Integer>> goalsByCharacter = new HashMap<Character, HashSet<Integer>>();
//    public static HashMap<Node, HashMap<Coordinates, Character>> boxesByNodes = new HashMap<Node, HashMap<Coordinates, Character>>();

    private Level() {
    }

    public static void addWall(int x, int y) {
        walls.add(Coordinates.hashCode(x, y));
    }

    public static void addGoal(int x, int y, char goalLetter) {
        Integer coordinates = Coordinates.hashCode(x, y);

        goalsByCoordinates.put(coordinates, goalLetter);

        if(goalsByCharacter.containsKey(goalLetter)){
            goalsByCharacter.get(goalLetter).add(coordinates);
        }
        else{
            HashSet<Integer> newCoordinates = new HashSet<Integer>();
            newCoordinates.add(coordinates);
            goalsByCharacter.put(goalLetter, newCoordinates);
        }
    }

//    public static void addNode(Node n){
//        boxesByNodes.put(n, new HashMap<Coordinates, Character>());
//    }
//
//    public static HashMap<Coordinates, Character> getBoxes(Node n){
//        return boxesByNodes.get(n);
//    }

//    public static void add(Node n, )

    public static char getGoal(int x, int y) {
        return goalsByCoordinates.get(Coordinates.hashCode(x, y));
    }

    public static boolean hasGoal(int x, int y) {
        return goalsByCoordinates.containsKey(Coordinates.hashCode(x, y));
    }

    public static boolean hasWall(int x, int y) {
        return walls.contains(Coordinates.hashCode(x, y));
    }

    public static Set<Character> getGoals() {
        return goalsByCharacter.keySet();
    }

    public static HashSet<Integer> getGoalCoordinates(char goalLetter) {
        return goalsByCharacter.get(Character.toLowerCase(goalLetter));
    }
}
