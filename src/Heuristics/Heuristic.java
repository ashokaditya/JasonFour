package Heuristics;

import DataStructures.Coordinates;
import DataStructures.Level;
import DataStructures.Node;

import java.util.*;

public abstract class Heuristic implements Comparator<Node> {

    public Node initialState;
//    HashMap<Character, int[]> goals = new HashMap<Character, int[]>();

    public Heuristic(Node initialState) {
        this.initialState = initialState;
//
//        for (int i = 0; i < initialState.MAX_ROW; i++) {
//            for (int j = 0; j < initialState.MAX_COLUMN; j++) {
//                char content = Level.getGoal(i, j);
//                int[] coordinates = new int[2];
//                if ('a' <= content && content <= 'z') {
//                    coordinates[0] = i;
//                    coordinates[1] = j;
//                    goals.put(content, coordinates);
//                }
//            }
//        }
    }

    public int compare(Node n1, Node n2) {
        return f(n1) - f(n2);
    }

    public int h(Node n) {
//        HashMap<Character, List<int[]>> boxes = new HashMap<Character, List<int[]>>();
//
//        for (int i = 0; i < n.MAX_ROW; i++) {
//            for (int j = 0; j < n.MAX_COLUMN; j++) {
//
//                char content = n.getBoxLetter(i, j);//n.boxes[i][j];
//                int[] coordinates = new int[2];
//
//                if ('A' <= content && content <= 'Z') {
//                    coordinates[0] = i;
//                    coordinates[1] = j;
//                    if (!boxes.containsKey(content)) {
//                        boxes.put(content, new LinkedList<int[]>());
//                    }
//                    boxes.get(content).add(coordinates);
//                }
//            }
//        }

        int totalDistance = 0;
        int minDistance = Integer.MAX_VALUE;

        for (Character goalLetter : Level.getGoals()) {
            HashSet<Coordinates> goalsCoordinates = Level.getGoalCoordinates(goalLetter);

            for (Coordinates boxCoordinates : n.getBoxes(goalLetter)) {
                for(Coordinates goalCoordinates : goalsCoordinates){
                    int distance = boxCoordinates.manhattanDistanceTo(goalCoordinates);
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }

            totalDistance += minDistance;
        }

        return totalDistance;
    }

    public abstract int f(Node n);
}
