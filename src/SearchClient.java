import DataStructures.Coordinates;
import DataStructures.Level;
import DataStructures.Memory;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SearchClient {

    public static void error(String msg) throws Exception {
        throw new Exception("GSCError: " + msg);
    }

    public Node initialState = null;

    public SearchClient(BufferedReader serverMessages) throws Exception {
        Map<Character, String> colors = new HashMap<Character, String>();
        String line, color;

        int agentCol = -1, agentRow = -1;
        int colorLines = 0, levelLines = 0;

        // Read lines specifying colors
        while ((line = serverMessages.readLine()).matches("^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$")) {
            line = line.replaceAll("\\s", "");
            String[] colonSplit = line.split(":");
            color = colonSplit[0].trim();

            for (String id : colonSplit[1].split(",")) {
                colors.put(id.trim().charAt(0), color);
            }
            colorLines++;
        }

//		if ( colorLines > 0 ) {
//			error( "Box colors not supported" );
//		}

        initialState = new Node(null);

        boolean[][] walls = new boolean[70][70];
        char[][] boxes = new char[70][70];
        char[][] goals = new char[70][70];
        int maxRows = 0;
        int maxCols = 0;

        while (!line.equals("")) {
            for (int i = 0; i < line.length(); i++) {
                char chr = line.charAt(i);
                if ('+' == chr) { // Walls
                    Level.addWall(levelLines, i);
                    maxRows = levelLines + 1;
                    if (maxCols <= i) {
                        maxCols = i + 1;
                    }
                } else if ('0' <= chr && chr <= '9') { // Agents
                    if (agentCol != -1 || agentRow != -1) {
                        error("Not a single agent level");
                    }
                    initialState.agentRow = levelLines;
                    initialState.agentCol = i;
                } else if ('A' <= chr && chr <= 'Z') { // Boxes
                    initialState.addBox(levelLines, i, chr);
                } else if ('a' <= chr && chr <= 'z') { // Goal cells
                    Level.addGoal(levelLines, i, chr);
                }
            }
            line = serverMessages.readLine();
            levelLines++;
        }

        initialState.MAX_COLUMN = maxCols;
        initialState.MAX_ROW = maxRows;
    }

    public LinkedList<Node> Search(Strategy strategy) throws IOException {
        System.err.format("Search starting with strategy %s\n", strategy);
        strategy.addToFrontier(this.initialState);

        int iterations = 0;
        while (true) {
            if (iterations % 200 == 0) {
                System.err.println(strategy.searchStatus());
            }
            if (Memory.shouldEnd()) {
                System.err.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep());
                return null;
            }
            if (strategy.timeSpent() > 300) { // Minutes timeout
                System.err.format("Time limit reached, terminating search %s\n", Memory.stringRep());
                return null;
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            Node leafNode = strategy.getAndRemoveLeaf();

            if (leafNode.isGoalState()) {
                return leafNode.extractPlan();
            }

            strategy.addToExplored(leafNode);
            ArrayList<Node> expandedNodes = leafNode.getExpandedNodes();
            for (Node n : expandedNodes) {
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Read level and create the initial state of the problem
        SearchClient client = new SearchClient(serverMessages);

        Strategy strategy = null;
        if (args.length == 0) {
            strategy = new StrategyBestFirst(new AStar(client.initialState));
        } else {
            String cmdParameter = args[0];
            if (cmdParameter.equalsIgnoreCase("dfs")) {
                strategy = new StrategyDFS();
            } else if (cmdParameter.equalsIgnoreCase("bfs")) {
                strategy = new StrategyBFS();
            } else if (cmdParameter.equalsIgnoreCase("astar")) {
                strategy = new StrategyBestFirst(new AStar(client.initialState));
            } else if (cmdParameter.equalsIgnoreCase("wastar")) {
                strategy = new StrategyBestFirst(new WeightedAStar(client.initialState));
            } else if (cmdParameter.equalsIgnoreCase("greedy")) {
                strategy = new StrategyBestFirst(new Greedy(client.initialState));
            } else {
                System.err.println("Unrecognized strategy - " + cmdParameter + ". Try with - DFS, BFS, AStar, WAStar, Greedy");
                System.exit(0);
            }
        }

        LinkedList<Node> solution = client.Search(strategy);

        if (solution == null) {
            System.err.println("Unable to solve level");
            System.exit(0);
        } else {
            System.err.println("\nSummary for " + strategy);
            System.err.println("Found solution of length " + solution.size());
            System.err.println(strategy.searchStatus());

            for (Node n : solution) {
                String act = n.action.toActionString();
                System.out.println(act);
                String response = serverMessages.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
                    System.err.format("%s was attempted in \n%s\n", act, n);
                    break;
                }
            }
        }
    }
}
