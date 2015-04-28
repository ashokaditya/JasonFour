package DataStructures;

import java.util.*;
import java.util.stream.Collector;

public final class Level {
    private static Set<Integer> walls = new HashSet<Integer>();
    private static Map<Character, Color> colors = new HashMap<Character, Color>();

    private static Map<Integer, Character> boxes = new HashMap<Integer, Character>();
//    private static Map<Color, LinkedList<Integer>> boxesByColor = new HashMap<Color, LinkedList<Integer>>();
    private static Map<Character, LinkedList<Integer>> boxesByCharacter = new HashMap<Character, LinkedList<Integer>>();
    private static Set<Integer> takenBoxes = new HashSet<Integer>();

    private static Map<Integer, Goal> goalsByCoordinates2 = new HashMap<Integer, Goal>();
    private static Map<Character, LinkedList<Goal>> goalsByCharacter2 = new HashMap<Character, LinkedList<Goal>>();

    private static Map<Integer, Character> agents = new HashMap<Integer, Character>();
    private static Map<Character, Integer> agentsByName = new HashMap<Character, Integer>();

    private Level() {
    }

    public static void addWall(int x, int y) {
        walls.add(Coordinates.hashCode(x, y));
    }

    public static void addGoal(int x, int y, char goalLetter) {
        Integer coordinates = Coordinates.hashCode(x, y);

        Goal goal = new Goal(coordinates, goalLetter, Status.FREE);

        goalsByCoordinates2.put(coordinates, goal);

        if(goalsByCharacter2.containsKey(goalLetter)){
            goalsByCharacter2.get(goalLetter).add(goal);
        }
        else{
            LinkedList<Goal> newGoals = new LinkedList<Goal>();
            newGoals.add(goal);
            goalsByCharacter2.put(goalLetter, newGoals);
        }
    }

    public static void addObjectColor(Character object, Color color){
        colors.put(object, color);
    }

    public static void addAgent(int row, int col, char chr) {

        if(!colors.containsKey(chr)){
            colors.put(chr, Color.BLUE);
        }

        Integer coordinates = Coordinates.hashCode(row, col);
        agents.put(coordinates, chr);
        agentsByName.put(chr, coordinates);
    }

    public static void addBox(int row, int col, char chr) {
        Integer hashCoordinates = Coordinates.hashCode(row, col);
        boxes.put(hashCoordinates, chr);

        if(!colors.containsKey(chr)){
            colors.put(chr, Color.BLUE);
        }

//        Color boxColor = colors.get(chr);

//        if(!boxesByColor.containsKey(boxColor)){
//            boxesByColor.put(boxColor, new LinkedList<Integer>());
//        }
        if(!boxesByCharacter.containsKey(chr)){
            boxesByCharacter.put(chr, new LinkedList<Integer>());
        }
//        boxesByColor.get(boxColor).add(hashCoordinates);
        boxesByCharacter.get(chr).add(hashCoordinates);
    }

    public static boolean hasGoal(int x, int y) {
        return goalsByCoordinates2.containsKey(Coordinates.hashCode(x, y));
    }

    public static boolean hasWall(int x, int y) {
        return walls.contains(Coordinates.hashCode(x, y));
    }

    public static void update(Character agentName, Command direction){

        int agentHashCoordinates = agentsByName.get(agentName);

        moveAgent(agentHashCoordinates, direction.dir1);
        if(direction.actType == Command.type.Pull){
            int boxHashCoordinates = GetCoordinates(agentHashCoordinates, direction.dir2);
            moveBox(boxHashCoordinates, Command.GetOpposite(direction.dir2));
        }
        else if(direction.actType == Command.type.Push){
            int boxHashCoordinates = GetCoordinates(agentHashCoordinates, direction.dir1);
            moveBox(boxHashCoordinates, direction.dir2);
        }
    }

    private static void moveBox(int boxHashCoordinates, Command.dir dir) {
        Character boxLetter = boxes.remove(boxHashCoordinates);
        boxesByCharacter.get(boxLetter).removeFirstOccurrence(boxHashCoordinates);

        int newCoordinates = GetCoordinates(boxHashCoordinates, dir);
        boxesByCharacter.get(boxLetter).add(newCoordinates);
        boxes.put(newCoordinates, boxLetter);
        takenBoxes.remove(boxHashCoordinates);
        takenBoxes.add(newCoordinates);

        // set as satisfied if a box is moved to the goal
        if(goalsByCoordinates2.containsKey(newCoordinates) &&
                goalsByCoordinates2.get(newCoordinates).Letter.equals(Character.toLowerCase(boxLetter))){
            goalsByCoordinates2.get(newCoordinates).Status = Status.SATISFIED;
        }
    }

    private static void moveAgent(int agentHashCoordinates, Command.dir dir) {
        Character agentName = agents.remove(agentHashCoordinates);
        int newCoordinates = GetCoordinates(agentHashCoordinates, dir);
        agentsByName.put(agentName, newCoordinates);
        agents.put(newCoordinates, agentName);
    }

    //TODO: could be done faster
    private static int GetCoordinates(int agentHashCoordinates, Command.dir direction) {
        Coordinates agent = new Coordinates(agentHashCoordinates);
        if(direction == Command.dir.E){
            return new Coordinates(agent.getRow(), agent.getCol()+1).hashCode();
        }
        if(direction == Command.dir.W){
            return new Coordinates(agent.getRow(), agent.getCol()-1).hashCode();
        }
        if(direction == Command.dir.N){
            return new Coordinates(agent.getRow()-1, agent.getCol()).hashCode();
        }
        return new Coordinates(agent.getRow()+1, agent.getCol()).hashCode();
    }

    public static char getGoal(int x, int y) { return goalsByCoordinates2.get(Coordinates.hashCode(x, y)).Letter; }

    public static boolean sameColors(Character object1, Character object2) {
        return colors.get(object1).equals(colors.get(object2));
    }

    public static Map<Integer, Character> getAgents() {
        return agents;
    }

    public static Map<Integer, Character> getBoxes(){
        return boxes;
    }

    public static Integer getGoalFor(Character agentName){
        Color agentColor = colors.get(agentName);
        List<Goal> goalsOfAgentColor = GetGoalsOfColor(agentColor);

        //TODO: get suitable goal instead
        for(Goal goal : goalsOfAgentColor){
            if(goal.Status == Status.FREE){
                goal.Status = Status.TAKEN;
                return goal.HashCoordinates;
            }
        }

        return -1;
//        return goalsByCharacter.get(goalLetter).pop();
    }

    public static Integer getBoxFor(Character agentName, int goalHashCoordinates){
        char goalLetter = goalsByCoordinates2.get(goalHashCoordinates).Letter;
//        char goalLetter = goalsByCoordinates.get(goalHashCoordinates);
        char boxLetter = Character.toUpperCase(goalLetter);

        //TODO: get suitable box instead
        for(int boxHashCoordinates : boxesByCharacter.get(boxLetter)){
            if(!takenBoxes.contains(boxHashCoordinates)){
                takenBoxes.add(boxHashCoordinates);
                return boxHashCoordinates;
            }
        }
        return -1;
    }

    public static void setGoalFree(int hashCoordinates){
        goalsByCoordinates2.get(hashCoordinates).Status = Status.FREE;
    }

    private static List<Goal> GetGoalsOfColor(Color targetColor) {
        List<Goal> goals = new LinkedList<Goal>();

        for (Map.Entry<Character, Color> entry : colors.entrySet()){
            Color objectColor = entry.getValue();
            Character goalLetter = Character.toLowerCase(entry.getKey());

            if(Character.isDigit(goalLetter)){
                continue;
            }

            if(objectColor == targetColor){
                if(goalsByCharacter2.containsKey(goalLetter)) {
                    goals.addAll(goalsByCharacter2.get(goalLetter));
                }
            }
        }

        return goals;
    }

}
