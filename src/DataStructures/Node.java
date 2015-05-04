package DataStructures;

import java.util.*;

public class Node {

    public static int MAX_ROW = 70;
    public static int MAX_COLUMN = 70;

    public int agentRow;
    public int agentCol;
    public int agentHashCoordinates;

    public Node parent;
    public Command action;

    //TODO: is it possible to save space ?
    private HashMap<Integer, Character> boxes = new HashMap<Integer, Character>();
    private HashMap<Character, HashSet<Integer>> boxesByCharacter = new HashMap<Character, HashSet<Integer>>();
//    private HashMap<Coordinates, Character> boxes = new HashMap<Coordinates, Character>();
//    private HashMap<Character, HashSet<Coordinates>> boxesByCharacter = new HashMap<Character, HashSet<Coordinates>>();

    public int goal;
    public int box;

    private int g;
    public HashMap<Integer, Character> agents = new HashMap<Integer, Character>();

    public Node(Node parent) {
        this.parent = parent;
        if (parent == null) {
            g = 0;
        } else {
            g = parent.g() + 1;
        }
    }

    public int g() {
        return g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {
        return box == goal;

//        for (Character goalLetter : boxesByCharacter.keySet()) {
//            HashSet<Integer> boxesSet = this.getBoxes(goalLetter);
////            HashSet<Integer> goalsSet = Level.getGoalCoordinates(goalLetter);
//
//            for(Integer goalHash : Level.getGoalCoordinates(goalLetter)){
//                if(!boxesSet.contains(goalHash)){
//                    return false;
//                }
//            }
//        }
//
//        return true;
    }

    public ArrayList<Node> getExpandedNodes() {
        ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.every.length);
        for (Command command : Command.every) {
            // Determine applicability of action
//            int newAgentRow = this.agentRow + dirToRowChange(command.dir1);
//            int newAgentCol = this.agentCol + dirToColChange(command.dir1);

            int newAgentHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir1);

            if(command.actType == Command.type.Move){
                if(this.hasFreeCellAt(newAgentHashCoordinates)){
                    expandedNodes.add(this.ChildNode(command));
                }
            }
            else if(command.actType == Command.type.Pull){

                if(this.hasFreeCellAt(newAgentHashCoordinates)) {
                    int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir2);

                    char agentName = agents.get(agentHashCoordinates);

                    if (this.hasBoxAt(boxHashCoordinates) && Level.sameColor(agentName, getBoxLetter(boxHashCoordinates))) {
                        expandedNodes.add(this.ChildNode(command));
                    }
                }
            }
            else if(command.actType == Command.type.Push){
                int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir1);
                char agentName = agents.get(agentHashCoordinates);

                if(this.hasBoxAt(boxHashCoordinates) && Level.sameColor(agentName, getBoxLetter(boxHashCoordinates))){

                    int newBoxHashCoordinates = Coordinates.move(boxHashCoordinates, command.dir2);
                    if(this.hasFreeCellAt(newBoxHashCoordinates)){
                        expandedNodes.add(this.ChildNode(command));
                    }
                }
            }

//            if (command.actType == Command.type.Move) {
//                // Check if there's a wall or box on the cell to which the agent is moving
//                if (this.hasFreeCellAt(newAgentRow, newAgentCol)) {
//                    Node n = this.ChildNode();
//                    n.action = command;
//                    n.agentRow = newAgentRow;
//                    n.agentCol = newAgentCol;
//                    expandedNodes.add(n);
//                }
//            } else if (command.actType == Command.type.Push) {
//                // Make sure that there's actually a box to move
//                if (this.hasBoxAt(newAgentRow, newAgentCol)) {
//                    int newBoxRow = newAgentRow + dirToRowChange(command.dir2);
//                    int newBoxCol = newAgentCol + dirToColChange(command.dir2);
//                    // .. and that new cell of box is free
//                    if (this.hasFreeCellAt(newBoxRow, newBoxCol)) {
//
//                        // .. and it's of this agent's color
//
//                        Node n = this.ChildNode();
//                        n.action = command;
//                        n.moveAgent(newAgentRow, newAgentCol);
//                        n.moveBox(newAgentRow, newAgentCol, newBoxRow, newBoxCol);
//                        expandedNodes.add(n);
//                    }
//                }
//            } else if (command.actType == Command.type.Pull) {
//                // Cell is free where agent is going
//                if (hasFreeCellAt(newAgentRow, newAgentCol)) {
//                    int boxRow = this.agentRow + dirToRowChange(command.dir2);
//                    int boxCol = this.agentCol + dirToColChange(command.dir2);
//                    // .. and there's a box in "dir2" of the agent
//                    if (this.hasBoxAt(boxRow, boxCol)) {
//
//                        // .. and is in this agent's color
//
//                        Node n = this.ChildNode();
//                        n.action = command;
//                        n.moveAgent(newAgentRow, newAgentCol);
//                        n.moveBox(boxRow, boxCol, this.agentRow, this.agentCol);
//                        expandedNodes.add(n);
//                    }
//                }
//            }
        }
//        Collections.shuffle(expandedNodes, rnd);
        return expandedNodes;
    }

    private void moveBox(int oldBoxX, int oldBoxY, int newBoxX, int newBoxY) {
        Integer oldBoxCoordinates = Coordinates.hashCode(oldBoxX, oldBoxY);
        Integer newBoxCoordinates = Coordinates.hashCode(newBoxX, newBoxY);

        Character boxLetter = this.boxes.remove(oldBoxCoordinates);
        this.boxes.put(newBoxCoordinates, boxLetter);

        this.boxesByCharacter.get(boxLetter).remove(oldBoxCoordinates);
        this.boxesByCharacter.get(boxLetter).add(newBoxCoordinates);

        if(box == oldBoxCoordinates){
            box = newBoxCoordinates;
        }
    }

    private void moveAgent(int newAgentX, int newAgentY) {
        agentRow = newAgentX;
        agentCol = newAgentY;
    }

    private int moveBox(int boxHashCoordinates, Command.dir direction){
        Character boxLetter = this.boxes.remove(boxHashCoordinates);
        this.boxesByCharacter.get(boxLetter).remove(boxHashCoordinates);

        int newCoordinates = Coordinates.move(boxHashCoordinates, direction);
        this.boxesByCharacter.get(boxLetter).add(newCoordinates);
        this.boxes.put(newCoordinates, boxLetter);

        if(this.box == boxHashCoordinates){
            this.box = newCoordinates;
        }

//        takenBoxes.remove(boxHashCoordinates);
//        takenBoxes.add(newCoordinates);

        // set as satisfied if a box is moved to the goal
//        if(goalsByCoordinates2.containsKey(newCoordinates) &&
//                goalsByCoordinates2.get(newCoordinates).Letter.equals(Character.toLowerCase(boxLetter))){
//            goalsByCoordinates2.get(newCoordinates).Status = Status.SATISFIED;
//        }

        return newCoordinates;
    }

    private void moveAgent(int agentHashCoordinates, Command.dir dir) {
        Character agentName = agents.remove(agentHashCoordinates);
        int newCoordinates = Coordinates.move(agentHashCoordinates, dir);
        this.agentHashCoordinates = newCoordinates;
//        agentsByName.put(agentName, newCoordinates);
        agents.put(newCoordinates, agentName);
    }

    private boolean hasFreeCellAt(int cellHashCoordinates) {
        return (!Level.hasWall(cellHashCoordinates) && !this.hasBoxAt(cellHashCoordinates) && !this.hasAgentAt(cellHashCoordinates));
    }

    private boolean hasAgentAt(int cellHashCoordinates) {
        return agents.containsKey(cellHashCoordinates);
    }

    private boolean hasFreeCellAt(int row, int col) {
        //TODO: add agent
        return (!Level.hasWall(row, col) && !this.hasBoxAt(row, col));
    }

    private boolean hasBoxAt(int boxHashCoordinate) {
        return boxes.containsKey(boxHashCoordinate);
    }

    private boolean hasBoxAt(int x, int y) {
        return boxes.containsKey(Coordinates.hashCode(x, y));
    }

    private int dirToRowChange(Command.dir d) {
        return (d == Command.dir.S ? 1 : (d == Command.dir.N ? -1 : 0)); // South is down one row (1), north is up one row (-1)
    }

    private int dirToColChange(Command.dir d) {
        return (d == Command.dir.E ? 1 : (d == Command.dir.W ? -1 : 0)); // East is left one column (1), west is right one column (-1)
    }

    public Node ChildNode(Command command){
        return ChildNode(this.agentHashCoordinates, command);
    }

    public Node ChildNode(int agentHashCoordinates, Command command) {
        Node childNode = this.ChildNode();
        childNode.action = command;
        childNode.moveAgent(agentHashCoordinates, command.dir1);
        if(command.actType == Command.type.Pull){
            int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir2);
            childNode.moveBox(boxHashCoordinates, Command.GetOpposite(command.dir2));
        }
        else if(command.actType == Command.type.Push){
            int boxHashCoordinates = Coordinates.move(agentHashCoordinates, command.dir1);
            childNode.moveBox(boxHashCoordinates, command.dir2);
        }

        return childNode;
    }

    private Node ChildNode() {
        Node copy = new Node(this);

        copy.boxes = (HashMap<Integer, Character>) this.boxes.clone();
        copy.boxesByCharacter = new HashMap<Character, HashSet<Integer>>();
        copy.box = this.box;
        copy.goal = this.goal;
        copy.agentHashCoordinates = this.agentHashCoordinates;
        copy.agents = (HashMap<Integer, Character>) this.agents.clone();

        for (char c : this.boxesByCharacter.keySet()){
            copy.boxesByCharacter.put(c, (HashSet<Integer>)this.boxesByCharacter.get(c).clone());
        }

        return copy;
    }

    public LinkedList<Node> extractPlan() {
        LinkedList<Node> plan = new LinkedList<Node>();
        Node n = this;
        while (!n.isInitialState()) {
            plan.addFirst(n);
            n = n.parent;
        }
        return plan;
    }

    @Override
    public int hashCode() {

        //TODO: make this faster

        final int prime = 31;
        int result = 1;
        result = prime * result + agentHashCoordinates;
        result = prime * result + box;
//        result = prime * result + agentCol;
//        result = prime * result + agentRow;
        for(Integer x : this.boxes.keySet()){
            result = prime * result + x.hashCode();
        }
        //TODO: ???
//        result = prime * result + Arrays.deepHashCode(boxes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        //TODO: make this faster

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if(agentHashCoordinates != other.agentHashCoordinates)
            return false;
        if(box != other.box){
            return false;
        }
//        if (agentCol != other.agentCol)
//            return false;
//        if (agentRow != other.agentRow)
//            return false;
        //TODO: does it work ?
        if (!boxes.equals(other.boxes)) {
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < MAX_ROW; row++) {
            if (!Level.hasWall(row, 0)) {
                break;
            }
            for (int col = 0; col < MAX_COLUMN; col++) {
                if (this.hasBoxAt(row, col)) {
                    s.append(this.getBoxLetter(row, col));
                } else if (Level.hasGoal(row, col)) {
                    s.append(Level.getGoal(row, col));
                } else if (Level.hasWall(row, col)) {
                    s.append("+");
                } else if (row == this.agentRow && col == this.agentCol) {
                    s.append("0");
                } else {
                    s.append(" ");
                }
            }

            s.append("\n");
        }
        return s.toString();
    }

    public char getBoxLetter(int x, int y) {
        return boxes.get(Coordinates.hashCode(x, y));
    }

    public void addBox(int x, int y, char boxLetter) {
        Integer coordinates = Coordinates.hashCode(x, y);

        this.boxes.put(Coordinates.hashCode(x, y), boxLetter);

        if(this.boxesByCharacter.containsKey(boxLetter)){
            this.boxesByCharacter.get(boxLetter).add(coordinates);
        }
        else{
            HashSet<Integer> newCoordinates = new HashSet<Integer>();
            newCoordinates.add(coordinates);
            this.boxesByCharacter.put(boxLetter, newCoordinates);
        }
    }

    public HashMap<Integer, Character> getBoxes() {
        return this.boxes;
    }

    public HashSet<Integer> getBoxes(char boxLetter) {
        return this.boxesByCharacter.get(Character.toUpperCase(boxLetter));
    }

    public Set<Character> getAllBoxLetters(){
        return boxesByCharacter.keySet();
    }

    public void setDedicatedGoal(Integer boxHashCoordinates, Integer goalHashCoordinates){
        box = boxHashCoordinates;
        goal = goalHashCoordinates;
    }

    public void addBox(Integer key, Character value) {
        Coordinates coord = new Coordinates(key);
        addBox(coord.getRow(), coord.getCol(), value);
    }

    public Character getBoxLetter(int boxHashCoordinates) {
        return boxes.get(boxHashCoordinates);
    }

    public Character getAgentName(int agentHashCoordinates) {
        return this.agents.get(agentHashCoordinates);
    }

    public void addAgent(Integer agentHashCoordinates, Character agentName) {
        this.agents.put(agentHashCoordinates, agentName);
    }

    public HashMap<Integer, Character> getAgents() {
        return agents;
    }
}