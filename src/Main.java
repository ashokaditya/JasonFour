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

public class Main {

    private static boolean fromFile = false;

    private static int[] countFrontier = new int[10];
    private static int[] countExplored = new int[10];
    private static int totalSolutionLength;

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = GetInputSource(args);

        // Read level and create the initial state of the problem
        ReadInput(serverMessages);

        HashMap<Character, List<Node>> solutions = new HashMap<Character, List<Node>>();

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
        int agentHashCoordinates = Level.getAgents().get(agentName);

        Node initialState = new Node(null);

        Coordinates agentCoordinates = new Coordinates(agentHashCoordinates);
        initialState.agentRow = agentCoordinates.getRow();
        initialState.agentCol = agentCoordinates.getCol();
        initialState.agentHashCoordinates = agentHashCoordinates;

        Integer goal = Level.getGoalFor(agentName);
        if (goal == -1) {
            return null;
        }
        Integer box = Level.getBoxFor(agentName, goal);

        for (Map.Entry<Integer, Character> entry2 : Level.getBoxes().entrySet()) {
            initialState.addBox(entry2.getKey(), entry2.getValue());
        }

        for (Map.Entry<Character, Integer> entry2 : Level.getAgents().entrySet()) {
            initialState.addAgent(entry2.getValue(), entry2.getKey());
        }

        initialState.setDedicatedGoal(box, goal);

        SearchClient client = new SearchClient(initialState);
        Strategy strategy = getStrategy(initialState, args);
        List<Node> solution = client.Search(strategy);

        countExplored[agentName - '0'] += strategy.explored.size();
        countFrontier[agentName - '0'] += strategy.countFrontier();

        return solution;
    }

    private static void ExecutePlans(HashMap<Character, List<Node>> solutions) {

//        Boolean solved = false;
//        int solutionLength = 0;
//        for (List<Node> list : solutions.values()) {
//            if (list != null) {
//                solved = true;
//                if (list.size() > solutionLength) {
//                    solutionLength = list.size();
//                }
//            }
//        }
//        if (!solved) {
//            System.err.println("Unable to solve level");
//            System.exit(0);
//        } else {
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

            //                String response = serverMessages.readLine();
            //                if (response.contains("false")) {
            //                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, jointAction);
            //                    System.err.format("%s was attempted in \n%s\n", jointAction, n);
            //                    break;
            //                }
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

    private static void PrintTotals() {

        PrintStream printStream;

        if(fromFile){
            printStream = System.out;
        }
        else{
            printStream = System.err;
        }

        printStream.format("Solution length: %14d\n\n", totalSolutionLength);
        printStream.println("          Explored     Frontier");

        for (char agentName : Level.getAgentNames()){
            printStream.format("Agent %c: %9d    %9d\n", agentName, countExplored[agentName - '0'], countFrontier[agentName - '0']);
        }
    }
}
