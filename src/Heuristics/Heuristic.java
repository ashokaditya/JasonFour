package Heuristics;

import DataStructures.Coordinates;
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
//        int agentHashCoordinates = Coordinates.hashCode(n.agentRow, n.agentCol);

        int h = Coordinates.manhattanDistance(n.box, n.goal)+ Coordinates.manhattanDistance(n.agentHashCoordinates, n.box);

        Set<Character> allLetters = initialState.getAllBoxLetters();

        int counter = 0;

        for (Character letter : allLetters){
            Set<Integer> coordinatesForLetter = initialState.getBoxes(letter);

            for (Integer coordinates : coordinatesForLetter){
                if(!n.getBoxes(letter).contains(coordinates)){
                    counter++;
                }
            }
        }

        if(initialState.box != n.box){
            counter--;
        }

        return h + counter*10;
    }

    public abstract int f(Node n);
}
