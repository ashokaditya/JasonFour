package Heuristics;

import DataStructures.Coordinates;
import DataStructures.Level;
import DataStructures.Node;

import java.util.*;

public abstract class Heuristic implements Comparator<Node> {

    public Node initialState;

    public Heuristic(Node initialState) {
        this.initialState = initialState;
    }

    public int compare(Node n1, Node n2) {
        return f(n1) - f(n2);
    }

    public int h(Node n) {

        int h = Coordinates.manhattanDistance(n.box, n.goal) +
                Coordinates.manhattanDistance(n.agentHashCoordinates, n.box) - 1;
        return h;

//        int totalDistance = 0;
//        int minDistance = Integer.MAX_VALUE;
//
//        for (Character goalLetter : Level.getGoals()) {
//            for (Integer boxHash : n.getBoxes(goalLetter)) {
//                for(Integer goalHash : Level.getGoalCoordinates(goalLetter)){
//
////                    Coordinates boxCoordinates = new Coordinates(boxHash);
////                    Coordinates goalCoordinates = new Coordinates(goalHash);
//
////                    int distance = boxCoordinates.manhattanDistanceTo(goalCoordinates);
//                    int distance = Coordinates.manhattanDistance(boxHash, goalHash);
//                    if (distance < minDistance) {
//                        minDistance = distance;
//                    }
//                }
//            }
//
//            totalDistance += minDistance;
//        }
//
//        return totalDistance;
    }

    public abstract int f(Node n);
}
