package DataStructures;

import java.util.*;

public final class Level {
    private static Set<Integer> walls = new HashSet<Integer>();
    private static Map<Character, Color> colors = new HashMap<Character, Color>();

//    private static Map<Integer, Character> boxes = new HashMap<Integer, Character>();
//    private static Map<Color, LinkedList<Integer>> boxesByColor = new HashMap<Color, LinkedList<Integer>>();
//    private static Map<Character, LinkedList<Integer>> boxesByCharacter = new HashMap<Character, LinkedList<Integer>>();
    private static Set<Integer> takenBoxes = new HashSet<Integer>();

    private static Map<Integer, Goal> goalsByCoordinates2 = new HashMap<Integer, Goal>();
    private static Map<Character, LinkedList<Goal>> goalsByCharacter2 = new HashMap<Character, LinkedList<Goal>>();

//    private static Map<Integer, Character> agents = new HashMap<Integer, Character>();
    private static Map<Character, Integer> agentsByName = new HashMap<Character, Integer>();

    private static Node state = new Node(null);

    private Level() {
    }

    public static Map<Integer, Character> getAgents() {
        return state.getAgents();
    }

    public static Map<Integer, Character> getBoxes(){
        return state.getBoxes();
    }

    public static char getGoal(int row, int col) { return goalsByCoordinates2.get(Coordinates.hashCode(row, col)).Letter; }

    public static void addWall(int row, int col) {
        walls.add(Coordinates.hashCode(row, col));
    }

    public static void addGoal(int row, int col, char goalLetter) {
        Integer coordinates = Coordinates.hashCode(row, col);

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
//        agents.put(coordinates, chr);
        agentsByName.put(chr, coordinates);
        state.addAgent(coordinates, chr);
    }

    public static void addBox(int row, int col, char chr) {
        Integer hashCoordinates = Coordinates.hashCode(row, col);
//        boxes.put(hashCoordinates, chr);
//
        if(!colors.containsKey(chr)){
            colors.put(chr, Color.BLUE);
        }

////        Color boxColor = colors.get(chr);
//
////        if(!boxesByColor.containsKey(boxColor)){
////            boxesByColor.put(boxColor, new LinkedList<Integer>());
////        }
//        if(!boxesByCharacter.containsKey(chr)){
//            boxesByCharacter.put(chr, new LinkedList<Integer>());
//        }
////        boxesByColor.get(boxColor).add(hashCoordinates);
//        boxesByCharacter.get(chr).add(hashCoordinates);

        state.addBox(hashCoordinates, chr);
    }

    public static boolean hasGoal(int row, int col) {
        return goalsByCoordinates2.containsKey(Coordinates.hashCode(row, col));
    }

    public static boolean hasGoal(int goalHashCoordinates) {
        return goalsByCoordinates2.containsKey(goalHashCoordinates);
    }

    public static boolean hasWall(int wallHashCoordinate) {
        return walls.contains(wallHashCoordinate);
    }

    public static boolean hasWall(int row, int col) {
        return walls.contains(Coordinates.hashCode(row, col));
    }

    public static void update(Character agentName, Command command){

        int agentHashCoordinates = agentsByName.get(agentName);

        state.agentHashCoordinates = agentHashCoordinates;
        state = state.ChildNode(agentHashCoordinates, command);

        agentsByName.put(agentName, state.agentHashCoordinates);
//        moveAgent(agentHashCoordinates, command.dir1);
        if(command.actType == Command.type.Pull){
            int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir2);
            moveBox(boxHashCoordinates, Command.GetOpposite(command.dir2));
        }
        else if(command.actType == Command.type.Push){
            int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir1);
            moveBox(boxHashCoordinates, command.dir2);
        }
    }

    public static boolean sameColor(Character object1, Character object2) {
        return colors.get(object1).equals(colors.get(object2));
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

    public static Integer getBoxFor(Character agentName, Integer goalHashCoordinates){
        char goalLetter = goalsByCoordinates2.get(goalHashCoordinates).Letter;
//        char goalLetter = goalsByCoordinates.get(goalHashCoordinates);
        char boxLetter = Character.toUpperCase(goalLetter);

        //TODO: get suitable box instead
        for(int boxHashCoordinates : state.getBoxes(boxLetter)){
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

    private static void moveBox(int boxHashCoordinates, Command.dir dir) {
//        boxesByCharacter.get(boxLetter).removeFirstOccurrence(boxHashCoordinates);
//
        int newCoordinates = Coordinates.move(boxHashCoordinates, dir);
//        boxesByCharacter.get(boxLetter).add(newCoordinates);
//        boxes.put(newCoordinates, boxLetter);
        takenBoxes.remove(boxHashCoordinates);
        takenBoxes.add(newCoordinates);

        Character boxLetter = state.getBoxLetter(newCoordinates);
        // set as satisfied if a box is moved to the goal
        if(goalsByCoordinates2.containsKey(newCoordinates) &&
                goalsByCoordinates2.get(newCoordinates).Letter.equals(Character.toLowerCase(boxLetter))){
            goalsByCoordinates2.get(newCoordinates).Status = Status.SATISFIED;
        }
    }

    private static void moveAgent(int agentHashCoordinates, Command.dir dir) {
        Character agentName = state.getAgentName(agentHashCoordinates);
        int newCoordinates = Coordinates.move(agentHashCoordinates, dir);
        agentsByName.put(agentName, newCoordinates);
//        agents.put(newCoordinates, agentName);
    }
}
