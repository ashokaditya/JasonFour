package DataStructures;

import java.util.*;

public final class Level {
    private static Set<Integer> walls = new HashSet<Integer>();
    private static Map<Integer, Character> goalsByCoordinates = new HashMap<Integer, Character>();
    private static Map<Character, LinkedList<Integer>> goalsByCharacter = new HashMap<Character, LinkedList<Integer>>();
    private static Map<Character, Color> colors = new HashMap<Character, Color>();
    private static Map<Color, LinkedList<Integer>> boxesByColor = new HashMap<Color, LinkedList<Integer>>();
    private static Map<Integer, Character> agents = new HashMap<Integer, Character>();
    private static Map<Integer, Character> boxes = new HashMap<Integer, Character>();

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
            LinkedList<Integer> newCoordinates = new LinkedList<Integer>();
            newCoordinates.add(coordinates);
            goalsByCharacter.put(goalLetter, newCoordinates);
        }
    }

    public static void addObjectColor(Character object, Color color){
        colors.put(object, color);
    }

//    public static void addNode(Node n){
//        boxesByNodes.put(n, new HashMap<Coordinates, Character>());
//    }
//
//    public static HashMap<Coordinates, Character> getBoxes(Node n){
//        return boxesByNodes.get(n);
//    }

//    public static void add(Node n, )

    public static boolean hasGoal(int x, int y) {
        return goalsByCoordinates.containsKey(Coordinates.hashCode(x, y));
    }

    public static boolean hasWall(int x, int y) {
        return walls.contains(Coordinates.hashCode(x, y));
    }

    public static char getGoal(int x, int y) {
        return goalsByCoordinates.get(Coordinates.hashCode(x, y));
    }

    public static Set<Character> getGoals() {
        return goalsByCharacter.keySet();
    }

    public static List<Integer> getGoalCoordinates(char goalLetter) {
        return goalsByCharacter.get(Character.toLowerCase(goalLetter));
    }

    public static boolean sameColors(Character object1, Character object2) {
        return colors.get(object1).equals(colors.get(object2));
    }

    public static void addAgent(int row, int col, char chr) {
        agents.put(Coordinates.hashCode(row, col), chr);
    }

    public static void addBox(int row, int col, char chr) {
        Integer hashCoordinates = Coordinates.hashCode(row, col);
        boxes.put(hashCoordinates, chr);
        Color boxColor = colors.get(chr);
        if(!boxesByColor.containsKey(boxColor)){
            boxesByColor.put(boxColor, new LinkedList<Integer>());
        }
        boxesByColor.get(boxColor).add(hashCoordinates);
    }

    public static Map<Integer, Character> getAgents() {
        return agents;
    }

    public static Integer getBoxFor(Character agentName) {
        Color agentColor = colors.get(agentName);
        LinkedList<Integer> boxesOfAgentColor = boxesByColor.get(agentColor);
        Integer boxHashCoordinates = boxesOfAgentColor.remove(0);
        return boxHashCoordinates;
    }

    public static Integer getGoalFor(Integer boxHashCoordinates) {
        Character boxLabel = boxes.get(boxHashCoordinates);
        Integer goalHashCoordinates = goalsByCharacter.get(Character.toLowerCase(boxLabel)).remove(0);
        return goalHashCoordinates;
    }

    public static Map<Integer, Character> getBoxes(){
        return boxes;
    }
}
