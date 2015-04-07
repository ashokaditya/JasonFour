import DataStructures.*;
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
import java.util.*;

public class SearchClient {

    public static void error(String msg) throws Exception {
        throw new Exception("GSCError: " + msg);
    }

    public static Node initialState = null;
    public static int agentNum = 0;
    public static int[] agentsRow = new int[10];
    public static int[] agentsCol = new int[10];

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
        int agentTemp = 0;

        for(int i = 0; i < 10; i++){
            agentsRow[i] = -1;
            agentsCol[i] = -1;
        }

        List<String> input = new ArrayList<String>();

        while (!line.equals("")) {
            input.add(line);
            line = serverMessages.readLine();
        }

        for(String currentLine : input){
            for (int i = 0; i < currentLine.length(); i++) {
                char chr = currentLine.charAt(i);
                if ('+' == chr) { // Walls
                    Level.addWall(levelLines, i);
//                    maxRows = levelLines + 1;
//                    if (maxCols <= i) {
//                        maxCols = i + 1;
//                    }
                } else if ('0' <= chr && chr <= '9') { // Agents
//                    if (agentCol != -1 || agentRow != -1) {
//                        error("Not a single agent level");
//                    }
//                    initialState.agentRow = levelLines;
//                    initialState.agentCol = i;

                    agentTemp = Character.getNumericValue(chr);
                    agentsRow[agentTemp] = levelLines;
                    agentsCol[agentTemp] = i;
                    agentNum++;

                } else if ('A' <= chr && chr <= 'Z') { // Boxes
                    initialState.addBox(levelLines, i, chr);
                } else if ('a' <= chr && chr <= 'z') { // Goal cells
                    Level.addGoal(levelLines, i, chr);
                }
            }
            levelLines++;
        }

//        initialState.MAX_COLUMN = maxCols;
//        initialState.MAX_ROW = maxRows;
    }

    public LinkedList<Node> Search(Strategy strategy) throws IOException {
        System.err.format("Search starting with strategy %s\n", strategy);
        strategy.addToFrontier(this.initialState);

        int iterations = 0;
        while (true) {
            if (iterations % 200 == 0) {
                System.err.println(strategy.searchStatus());
            }
//            if (Memory.shouldEnd()) {
//                System.err.format("Memory limit almost reached, terminating search %s\n", Memory.stringRep());
//                return null;
//            }
//            if (strategy.timeSpent() > 300) { // Minutes timeout
//                System.err.format("Time limit reached, terminating search %s\n", Memory.stringRep());
//                return null;
//            }

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

        List<List<Node>> solutions = new LinkedList<List<Node>>();

        Strategy strategy = null;
        for(int i = 0; i < agentNum; i++){
            initialState.agentRow = agentsRow[i];
            initialState.agentCol = agentsCol[i];
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
            solutions.add(client.Search(strategy));
        }

        Boolean solved = false;
        int solutionLength = 0;
        for (List<Node> list : solutions) {
            if(list != null){
                solved = true;
                if(list.size() > solutionLength){
                    solutionLength = list.size();
                }
            }
        }
        if (solved == false) {
            System.err.println("Unable to solve level");
            System.exit(0);
        } else {
            System.err.println();
            System.err.println("Summary for " + strategy);
            System.err.println("Found solution of length " + solutionLength);
            System.err.println(strategy.searchStatus());

//            System.exit(1);

            String jointAction;
            String response;
            Node n = new Node(null);
            for(int j = 0; j < solutionLength; j++){
                jointAction = "[";

                int i = 0;
                for (List<Node> list : solutions) {
                    //TODO noget hvor den kan poppe de forskellige elementer fra den linkedlist en efter en og smide dem ind sammen
                    // Det skal se således ud [agent1aktion,agent2aktion,agent3aktion]
                    //Evt skal der laves en klasse til hver agent
                    //Problemet kan opstå i en map hvor begge agenter går efter det samme eller dele af banen er skåret af for dem
                    //På den måde kan de ikke finde en solution..
                    //Som det er nu skal der kunne findes en solution selv hvis der kun er 1 agent i mappet

                    if(!list.isEmpty()){
                        n = list.remove(0);
                        jointAction += n.action.toString();
                    } else {
                        //TODO mellemstykker hvis det er empty
                    }

                    if(i + 1 != solutions.size()){
                        jointAction += ",";
                    }
                    i++;
                }

                jointAction += "]";

                System.out.println(jointAction);
                response = serverMessages.readLine();
                if (response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
                    System.err.format("%s was attempted in \n%s\n", jointAction, n);
                    break;
                }
            }
        }
    }
}
