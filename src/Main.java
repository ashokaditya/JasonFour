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

import java.io.*;
import java.util.*;

/**
 * Created by Administrator on 4/10/2015.
 */
public class Main {

    public static boolean fromFile = false;

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = getInputSource(args);

        // Use stderr to print to console
//        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Read level and create the initial state of the problem
        ReadInput(serverMessages);

        HashMap<Character, List<Node>> solutions = new HashMap<Character, List<Node>>();

        for (Map.Entry<Integer, Character> entry : Level.getAgents().entrySet()) {
            char agentName = entry.getValue();
            int agentHashCoordinates = entry.getKey();

            Node initialState = new Node(null);

            Coordinates agentCoordinates = new Coordinates(agentHashCoordinates);
            initialState.agentRow = agentCoordinates.getRow();
            initialState.agentCol = agentCoordinates.getCol();
            initialState.agentHashCoordinates = agentHashCoordinates;

            Integer goal = Level.getGoalFor(agentName);
            Integer box = Level.getBoxFor(agentName, goal);

            for (Map.Entry<Integer, Character> entry2 : Level.getBoxes().entrySet()) {
                initialState.addBox(entry2.getKey(), entry2.getValue());
            }

            for(Map.Entry<Integer, Character> entry2 : Level.getAgents().entrySet()) {
                initialState.addAgent(entry2.getKey(), entry2.getValue());
            }

            initialState.setDedicatedGoal(box, goal);

            Strategy str = getStrategy(initialState, args);
            SearchClient client = new SearchClient(initialState);
            solutions.put(initialState.getAgentName(agentHashCoordinates), client.Search(str));
            if (fromFile) {
                System.out.format("Explored for agent \"%c\": %d", agentName,  str.explored.size());
                System.out.println();
                System.out.format("Frontier for agent \"%c\": %d", agentName, str.countFrontier());
                System.out.println();
            }
        }

        Boolean solved = false;
        int solutionLength = 0;
        for (List<Node> list : solutions.values()) {
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
            if(fromFile) {
                System.out.println("Found solution of length " + solutionLength);
            }
            else {
//                System.err.println();
//                System.err.println("Summary for " + strategy);
                System.err.println("Found solution of length " + solutionLength);
//                System.err.println(strategy.searchStatus());
            }

            for (int j = 0; j < solutionLength; j++) {
                String jointAction = "[";

                int i = 0;
                for (Map.Entry<Character, List<Node>> entry : solutions.entrySet()) {
                    char agentName = entry.getKey();
                    List<Node> list = entry.getValue();

                    if (!list.isEmpty()) {
                        Node n = list.remove(0);
                        Level.update(agentName, n.action);
                        jointAction += n.action.toString();
                    } else {
                        jointAction += "NoOp";
                    }

                    if (i + 1 != solutions.size()) {
                        jointAction += ",";
                    }
                    i++;
                }

                jointAction += "]";
                if(!Main.fromFile){
                    System.out.println(jointAction);
                }
//                String response = serverMessages.readLine();
//                if (response.contains("false")) {
//                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
//                    System.err.format("%s was attempted in \n%s\n", jointAction, n);
//                    break;
//                }
            }
        }
    }

    private static BufferedReader getInputSource(String[] args) throws FileNotFoundException {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        if(args.length >= 1){
            for(String arg : args){
                if (arg.startsWith("-file=")){
                    serverMessages = new BufferedReader(new FileReader(arg.replace("-file=", "")));

                    fromFile = true;

                    System.setErr(new PrintStream(new OutputStream() {
                        public void write(int b) {
                        }
                    }));

                    break;
                }
            }
        }
        return serverMessages;
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
        while (currentLine != null && !currentLine.equals("")) {
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
            String alg = "astar";
            for (String cmdParameter : commandLineArguments) {
                if (cmdParameter.startsWith("-alg=")) {
                    alg = cmdParameter.replace("-alg=", "");
                    break;
                }
            }

            if (alg.equalsIgnoreCase("dfs")) {
                return new StrategyDFS();
            } else if (alg.equalsIgnoreCase("bfs")) {
                return new StrategyBFS();
            } else if (alg.equalsIgnoreCase("astar")) {
                return new StrategyBestFirst(new AStar(initialState));
            } else if (alg.equalsIgnoreCase("wastar")) {
                return new StrategyBestFirst(new WeightedAStar(initialState));
            } else if (alg.equalsIgnoreCase("greedy")) {
                return new StrategyBestFirst(new Greedy(initialState));
            } else {
                System.err.println("Unrecognized strategy - " + alg + ". Try with - DFS, BFS, AStar, WAStar, Greedy");
                System.exit(0);
            }
        }

        return new StrategyBestFirst(new AStar(initialState));
    }
}
