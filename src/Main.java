import DataStructures.Color;
import DataStructures.Coordinates;
import DataStructures.Level;
import DataStructures.Node;
import Heuristics.AStar;
import Heuristics.Greedy;
import Heuristics.WeightedAStar;
import Strategies.Strategy;
import Strategies.StrategyBFS;
import Strategies.StrategyBestFirst;
import Strategies.StrategyDFS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 4/10/2015.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Read level and create the initial state of the problem
        ReadInput(serverMessages);

        List<List<Node>> solutions = new LinkedList<List<Node>>();

        for (Map.Entry<Integer, Character> entry : Level.getAgents().entrySet()) {
            Node initialState = new Node(null);

            Coordinates agentHashCoordinates = new Coordinates(entry.getKey());
            initialState.agentRow = agentHashCoordinates.getRow();
            initialState.agentCol = agentHashCoordinates.getCol();

            Integer boxHashCoordinates = Level.getBoxFor(entry.getValue());
            for (Map.Entry<Integer, Character> entry2 : Level.getBoxes().entrySet()) {
                initialState.addBox(entry2.getKey(), entry2.getValue());
            }
            initialState.setDedicatedGoal(boxHashCoordinates, Level.getGoalFor(boxHashCoordinates));

            Strategy str = getStrategy(initialState, args);
            SearchClient client = new SearchClient(initialState);
            solutions.add(client.Search(str));
        }

        Boolean solved = false;
        int solutionLength = 0;
        for (List<Node> list : solutions) {
            if (list != null) {
                solved = true;
                if (list.size() > solutionLength) {
                    solutionLength = list.size();
                }
            }
        }
        if (solved == false) {
            System.err.println("Unable to solve level");
            System.exit(0);
        } else {
            System.err.println();
//            System.err.println("Summary for " + strategy);
            System.err.println("Found solution of length " + solutionLength);
//            System.err.println(strategy.searchStatus());

            String jointAction;
            Node n = new Node(null);
            for (int j = 0; j < solutionLength; j++) {
                jointAction = "[";

                int i = 0;
                for (List<Node> list : solutions) {

                    if (!list.isEmpty()) {
                        n = list.remove(0);
                        jointAction += n.action.toString();
                    } else {
                        //TODO mellemstykker hvis det er empty
                    }

                    if (i + 1 != solutions.size()) {
                        jointAction += ",";
                    }
                    i++;
                }

                jointAction += "]";

                System.out.println(jointAction);
//                String response = serverMessages.readLine();
//                if (response.contains("false")) {
//                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
//                    System.err.format("%s was attempted in \n%s\n", jointAction, n);
//                    break;
//                }
            }
        }
    }

    private static void ReadInput(BufferedReader serverMessages) throws IOException {
        final String colorDefinitionPattern = "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$";

        String currentLine = serverMessages.readLine();

        while (currentLine.matches(colorDefinitionPattern)) {
            currentLine = currentLine.replaceAll("\\s", "");
            String[] colonSplit = currentLine.split(":");
            Color color = Color.valueOf(colonSplit[0].trim().toUpperCase());

            for (String id : colonSplit[1].split(",")) {
                Level.addObjectColor(id.trim().charAt(0), color);
            }

            currentLine = serverMessages.readLine();
        }

        int row = 0;
        while (!currentLine.equals("")) {
            for (int col = 0; col < currentLine.length(); col++) {
                char chr = currentLine.charAt(col);
                if ('+' == chr) { // Walls
                    Level.addWall(row, col);
                } else if ('0' <= chr && chr <= '9') { // Agents
                    Level.addAgent(row, col, chr);
                } else if ('A' <= chr && chr <= 'Z') { // Boxes
                    Level.addBox(row, col, chr);
                } else if ('a' <= chr && chr <= 'z') { // Goal cells
                    Level.addGoal(row, col, chr);
                }
            }
            row++;
            currentLine = serverMessages.readLine();
        }
    }

    private static Strategy getStrategy(Node initialState, String[] commandLineArguments) {
        if (commandLineArguments.length >= 1) {
            String cmdParameter = commandLineArguments[0];
            if (cmdParameter.equalsIgnoreCase("dfs")) {
                return new StrategyDFS();
            } else if (cmdParameter.equalsIgnoreCase("bfs")) {
                return new StrategyBFS();
            } else if (cmdParameter.equalsIgnoreCase("astar")) {
                return new StrategyBestFirst(new AStar(initialState));
            } else if (cmdParameter.equalsIgnoreCase("wastar")) {
                return new StrategyBestFirst(new WeightedAStar(initialState));
            } else if (cmdParameter.equalsIgnoreCase("greedy")) {
                return new StrategyBestFirst(new Greedy(initialState));
            } else {
                System.err.println("Unrecognized strategy - " + cmdParameter + ". Try with - DFS, BFS, AStar, WAStar, Greedy");
                System.exit(0);
            }
        }

        return new StrategyBestFirst(new AStar(initialState));
    }
}
