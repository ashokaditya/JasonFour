import DataStructures.Color;
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

import java.io.*;
import java.util.*;

public class Main {

    private static boolean fromFile = false;

    private static int[] countFrontier = new int[10];
    private static int[] countExplored = new int[10];
    private static int totalSolutionLength;
    private static long startTime;

    private static BufferedReader serverMessages;

    public static float maxMemory;


    public static void main(String[] args) throws Exception {
        serverMessages = GetInputSource(args);

        // Read level and create the initial state of the problem
        ReadInput(serverMessages);

        HashMap<Character, List<Node>> solutions = new HashMap<Character, List<Node>>();

        startTime = System.currentTimeMillis();

        while (!Level.AreGoalsSatisfied()) {

            for (Character agentName : Level.getAgentNames()) {
                if (Level.isAgentFree(agentName)) {

                    Level.setAgentBusy(agentName);

                    List<Node> agentPlan = CreatePlan(agentName, args);

                    //if there is no solution or no goals
                    if (agentPlan == null) {
                        Level.setAgentFree(agentName);
                    }

                    solutions.put(agentName, agentPlan);
                }
            }

            ExecutePlans(solutions);
        }

        PrintTotals();
    }

    private static List<Node> CreatePlan(Character agentName, String[] args) throws IOException {
        Integer goal = Level.getGoalFor(agentName);
        if (goal == -1) {
            return null;
        }
        Integer box = Level.getBoxFor(agentName, goal);

        int agentHashCoordinates = Level.getAgents().get(agentName);

        Node initialState = new Node(null);
        initialState.agentHashCoordinates = agentHashCoordinates;
        initialState.updateFrom(Level.state);
        initialState.setDedicatedGoal(box, goal);

        System.err.println("Starting search with goal " + goal + " and box " + box);

        SearchClient client = new SearchClient(initialState);
        Strategy strategy = getStrategy(initialState, args);
        List<Node> solution = client.Search(strategy);

        countExplored[agentName - '0'] += strategy.explored.size();
        countFrontier[agentName - '0'] += strategy.countFrontier();

        return solution;
    }

    private static void ExecutePlans(HashMap<Character, List<Node>> solutions) throws IOException {

        Boolean needReplan = false;

        while (!needReplan) {
            List<String> jointAction = new LinkedList<String>();

            for (char agentName : Level.getAgentNames()) {
                List<Node> list = solutions.get(agentName);

                if (list != null && !list.isEmpty()) {
                    Node n = list.remove(0);
                    Level.update(agentName, n.action);
                    jointAction.add(n.action.toString());
                } else {
                    jointAction.add("NoOp");
                    needReplan = true;
                    Level.setAgentFree(agentName);
                    Level.setBoxFree(agentName);
                }
            }

            totalSolutionLength++;

            if (!fromFile) {
                System.out.format("[%s]\n", String.join(",", jointAction));
            }

//            String response = serverMessages.readLine();
//            if (response.contains("false")) {
//                System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
//                System.err.format("%s was attempted in \n%s\n", jointAction, Level.state);
//                break;
//            }
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

    private static BufferedReader GetInputSource(String[] args) throws FileNotFoundException {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));
        if (args.length >= 1) {
            for (String arg : args) {
                if (arg.startsWith("-file=")) {
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
            int col;
            for (col = 0; col < currentLine.length(); col++) {
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

            Level.MAX_COLUMN = Math.max(col, Level.MAX_COLUMN);

            row++;
            currentLine = serverMessages.readLine();
        }

        Level.MAX_ROW = row;
    }

    private static void PrintTotals() {

        PrintStream printStream;

        if (fromFile) {
            printStream = System.out;
        } else {
            printStream = System.err;
        }

        printStream.format("Solution length: %14d\n", totalSolutionLength);
        printStream.format("Time taken:     %14.2fs\n", (System.currentTimeMillis() - startTime) / 1000f);
        printStream.format("Max memory used:%12.2f MB\n", maxMemory);
        printStream.format("Encode counter:  %14d\n", Node.encodeCounter);
        printStream.println();
                printStream.println("          Explored     Frontier");

        for (char agentName : Level.getAgentNames()) {
            printStream.format("Agent %c: %9d    %9d\n", agentName, countExplored[agentName - '0'], countFrontier[agentName - '0']);
        }

//        printStream.println("");
//        printStream.println("Hash count: "+ Node.hashCount);
//        printStream.println("Equals count: "+ Node.equalsCount);
//        printStream.println("Failed count: "+ Node.failedEqualsCount);
    }
}
