package Strategies;

import DataStructures.Node;
import Heuristics.Heuristic;

import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Created by Administrator on 3/30/2015.
 */
public class StrategyBestFirst extends Strategy {

    private Heuristic heuristic;

    private HashSet<Node> front;
    private PriorityQueue<Node> frontier;

    public StrategyBestFirst( Heuristic h ) {
        super();
        heuristic = h;
        frontier = new PriorityQueue<Node>(100, h);
        front = new HashSet<Node>(100);
    }
    public Node getAndRemoveLeaf() {
        Node headNode = frontier.poll();
        front.remove(headNode);
        return headNode;
    }

    public void addToFrontier( Node n ) {
        frontier.add(n);
        front.add(n);
    }

    public int countFrontier() {
        return frontier.size();
    }

    public boolean frontierIsEmpty() {
        return frontier.isEmpty();
    }

    public boolean inFrontier( Node n ) {
        return front.contains(n);//frontier.contains(n);
    }

    public String toString() {
        return "Best-first Search (PriorityQueue) using " + heuristic.toString();
    }
}