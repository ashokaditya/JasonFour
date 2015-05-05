package DataStructures;

import java.lang.reflect.Array;
import java.util.*;

public final class Level {
    private static Set<Integer> walls = new HashSet<Integer>();
    private static Map<Character, Color> colors = new HashMap<Character, Color>();

//    private static Map<Integer, Character> boxes = new HashMap<Integer, Character>();
//    private static Map<Color, LinkedList<Integer>> boxesByColor = new HashMap<Color, LinkedList<Integer>>();
//    private static Map<Character, LinkedList<Integer>> boxesByCharacter = new HashMap<Character, LinkedList<Integer>>();
    private static Map<Integer, Character> takenBoxes = new HashMap<Integer, Character>();

//    private static Map<Integer, Box> boxes = new HashMap<Integer, Box>();

    private static Map<Integer, Goal> goalsByCoordinates2 = new HashMap<Integer, Goal>();
    private static Map<Character, LinkedList<Goal>> goalsByCharacter2 = new HashMap<Character, LinkedList<Goal>>();

    private static Map<Character, Queue<Goal>> goals;

//    private static Map<Integer, Character> agents = new HashMap<Integer, Character>();
    private static Map<Character, Integer> agentsByName = new HashMap<Character, Integer>();
    private static List<Character> agentNames = new LinkedList<Character>();
    private static Map<Character, Status> status = new HashMap<Character, Status>();

    private static Node state = new Node(null);

    private Level() {
    }

    public static Map<Character, Integer> getAgents() {
        return agentsByName;
    }

//    public static Map<Integer, Character> getAgents() {
//        return state.getAgents();
//    }

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

    public static void addAgent(int row, int col, char agentName) {

        if(!colors.containsKey(agentName)){
            colors.put(agentName, Color.BLUE);
        }

        Integer coordinates = Coordinates.hashCode(row, col);
//        agents.put(coordinates, chr);
        agentsByName.put(agentName, coordinates);
        state.addAgent(coordinates, agentName);
        status.put(agentName, Status.FREE);
    }

    public static void addBox(int row, int col, char boxLetter) {
        Integer hashCoordinates = Coordinates.hashCode(row, col);

        if(!colors.containsKey(boxLetter)){
            colors.put(boxLetter, Color.BLUE);
        }

//        Box box = new Box();
//        box.HashCoordinates = hashCoordinates;
//        box.Letter = boxLetter;
//        box.Status = Status.FREE;

//        boxes.put(box.HashCoordinates, box);

        state.addBox(hashCoordinates, boxLetter);
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

        if(goals == null){
            InstantiateGoalQueues();
        }

        Queue<Goal> agentQueue = goals.get(agentName);
        Integer goalCount = 0;

        Goal goal = agentQueue.poll();
        goalCount++;

        // keep searching for a goal, until you find unsatisfied
        // or you have tried all fo them
        while(goal.Status != Status.FREE){

            agentQueue.add(goal);

            if(goalCount > agentQueue.size()){
                return -1;
            }

            goal = agentQueue.poll();
            goalCount++;
        }

        goal.Status = Status.TAKEN;
        agentQueue.add(goal);

        return goal.HashCoordinates;
    }

    private static void InstantiateGoalQueues() {
        goals = new HashMap<Character, Queue<Goal>>();

        for(Character agentName : agentsByName.keySet()){
            Queue<Goal> goalQueue = prioritizeGoalsForAgent(agentName);

            goals.put(agentName, goalQueue);
        }
    }

    private static Queue<Goal> prioritizeGoalsForAgent(Character agentName) {
        Color agentColor = colors.get(agentName);
        return new ArrayDeque<Goal>(GetGoalsOfColor(agentColor));
    }

    public static Integer getBoxFor(Character agentName, Integer goalHashCoordinates){
        char goalLetter = goalsByCoordinates2.get(goalHashCoordinates).Letter;
//        char goalLetter = goalsByCoordinates.get(goalHashCoordinates);
        char boxLetter = Character.toUpperCase(goalLetter);

        //TODO: get closest box, which do not satisfy a goal with
        //TODO: bigger or equal priority

        for(int boxHashCoordinates : state.getBoxes(boxLetter)){
            if(!takenBoxes.containsKey(boxHashCoordinates)){
                takenBoxes.put(boxHashCoordinates, agentName);
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
        if(takenBoxes.containsKey(boxHashCoordinates)) {
            Character agentName = takenBoxes.remove(boxHashCoordinates);
            takenBoxes.put(newCoordinates, agentName);
        }

        Character boxLetter = state.getBoxLetter(newCoordinates);
        // set as satisfied if a box is moved to the goal
        if(goalsByCoordinates2.containsKey(newCoordinates) &&
                goalsByCoordinates2.get(newCoordinates).Letter.equals(Character.toLowerCase(boxLetter))){
            goalsByCoordinates2.get(newCoordinates).Status = Status.SATISFIED;
        }

        if(goalsByCoordinates2.containsKey(boxHashCoordinates)){
            goalsByCoordinates2.get(boxHashCoordinates).Status = Status.FREE;
            takenBoxes.remove(boxHashCoordinates);
        }

        // TODO: set as unsatisfied if a box is moved out of the goal
        // TODO: add it in the "goal queue"
    }

    private static void moveAgent(int agentHashCoordinates, Command.dir dir) {
        Character agentName = state.getAgentName(agentHashCoordinates);
        int newCoordinates = Coordinates.move(agentHashCoordinates, dir);
        agentsByName.put(agentName, newCoordinates);
//        agents.put(newCoordinates, agentName);
    }

    public static List<Character> getAgentNames(){

        if(agentNames.isEmpty()){
            agentNames.addAll(agentsByName.keySet());

            Collections.sort(agentNames);
        }

        return agentNames;
    }

    public static boolean AreGoalsSatisfied(){

        for (Goal goal : goalsByCoordinates2.values()){
            if(goal.Status != Status.SATISFIED){
                return false;
            }
        }

        return true;
    }

    public static boolean isAgentFree(Character agentName) {
        return status.get(agentName) == Status.FREE;
    }

    public static void setAgentBusy(Character agentName) {
        status.put(agentName, Status.TAKEN);
    }

    public static void setAgentFree(Character agentName) {
        status.put(agentName, Status.FREE);
    }

    //TODO: THIS IS AWFUL
    // it is setting a box free, but getting agentName for parameter ?? wtf
    public static void setBoxFree(Character agentName) {
            for (Map.Entry<Integer, Character> entry : takenBoxes.entrySet())
            {
                if(entry.getValue().equals(agentName)){
                    takenBoxes.remove(entry.getKey());
                    return;
                }
        }
    }

}
