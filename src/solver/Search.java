// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

import java.util.*;

public final class Search {

    public static final class Result {
        public Position endPosition;
        public ArrayList<Direction> path;
    }

    public static void floodfill(State state, boolean[][] map, int startX, int startY, boolean token) {
        /* Create stack to store nodes */
        Stack<Position> nodes = new Stack<Position>();
        /* Declare an positionobject to pop to from stack */
        Position currentPosition = new Position(startX, startY);

        /* Push the start position node, for the search on the stack */
        nodes.push(currentPosition);

        /* Search for a path to the wanted goal */
        while (!nodes.empty()) {
            currentPosition = nodes.pop();
            int x = currentPosition.x;
            int y = currentPosition.y;
            map[x][y] = token;

            /* Create child nodes */
            if (map[x + 1][y] != token && state.isFree(x + 1, y)) {
                nodes.add(new Position(x + 1, y));
            }
            if (map[x - 1][y] != token && state.isFree(x - 1, y)) {
                nodes.add(new Position(x - 1, y));
            }
            if (map[x][y + 1] != token && state.isFree(x, y + 1)) {
                nodes.add(new Position(x, y + 1));
            }
            if (map[x][y - 1] != token && state.isFree(x, y - 1)) {
                nodes.add(new Position(x, y - 1));
            }
        }
    }

    public static Result bfs(State state, SearchTest test, int startX, int startY) {
        /* Create stack to store nodes */
        ArrayDeque<Position> nodes = new ArrayDeque<Position>();
        /* Create integers that is needed */
        int width = state.getWidth();
        int height = state.getHeight();
        /* Create 2D array to store visited positions */
        VisitedNodes visitedPositions = VisitedNodes.createArray(width, height);
        /* Declare an positionobject to pop to from stack */
        Position currentPosition = new Position(startX, startY);

        /* Push the start position node, for the search on the stack */
        nodes.add(currentPosition);

        /* Search for a path to the wanted goal */
        while (!nodes.isEmpty()) {
            currentPosition = nodes.remove();
            int x = currentPosition.x;
            int y = currentPosition.y;

            if (test.isEnd(state, currentPosition.x, currentPosition.y)) {
                return createResult(visitedPositions, currentPosition, startX, startY);
            }

            /* Create child nodes */
            testAddPosition(state, test, nodes, visitedPositions, x, y - 1, Direction.UP);
            testAddPosition(state, test, nodes, visitedPositions, x, y + 1, Direction.DOWN);
            testAddPosition(state, test, nodes, visitedPositions, x - 1, y, Direction.LEFT);
            testAddPosition(state, test, nodes, visitedPositions, x + 1, y, Direction.RIGHT);
        }

        return null;
    }

    static Result createResult(VisitedNodes visited, Position currentPosition, int startX, int startY) {
        Result result = new Result();
        result.path = getPath(visited.directions, currentPosition.x, currentPosition.y, startX, startY);
        result.endPosition = new Position(currentPosition.x, currentPosition.y);
        return result;
    }

    private static ArrayList<Direction> getPath(Direction[][] moves, int fromX, int fromY, int toX, int toY) {
        int x = fromX, y = fromY;
        ArrayList<Direction> path = new ArrayList<Direction>();
        Position dir = new Position();
        while ((y != toY) | (x != toX)) {
            Direction move = moves[x][y];
            path.add(move);

            move.getDirection(dir);
            x -= dir.x;
            y -= dir.y;
        }
        return path;
    }

    private static void testAddPosition(State state, SearchTest test, Collection<Position> nodes, VisitedNodes visited,
            int x, int y, Direction move) {
        if (visited.get(x, y) == null && (state.isFree(x, y) || test.isEnd(state, x, y))) {
            Position possibleStep = new Position(x, y);
            visited.set(x, y, move);
            nodes.add(possibleStep);
        }
    }

    public static State findBoxPath(State state, int playerStartX, int playerStartY) {
        /* Create stack to store nodes */
        PriorityQueue<State> nodes = new PriorityQueue<State>();
        /* Declare an positionobject to pop to from stack */
        State currentState = state;
        currentState.player = new Position(playerStartX, playerStartY);

        /* Push the start position node, for the search on the stack */
        nodes.add(currentState);

        /* Search for a path to the wanted goal */
        while (!nodes.isEmpty()) {
            try {
                currentState = nodes.remove();

                if (currentState.isFinal()) {
                    return currentState;
                }

                /* Create child nodes */
                for (int boxIndex = 0; boxIndex < currentState.boxes.size(); ++boxIndex) {
                    Position box = currentState.boxes.get(boxIndex);
                    int x = box.x;
                    int y = box.y;

                    testBoxAddPosition(currentState, nodes, currentState.player, x, y - 1, x, y - 2, Direction.UP,
                            boxIndex);
                    testBoxAddPosition(currentState, nodes, currentState.player, x, y + 1, x, y + 2, Direction.DOWN,
                            boxIndex);
                    testBoxAddPosition(currentState, nodes, currentState.player, x - 1, y, x - 2, y, Direction.LEFT,
                            boxIndex);
                    testBoxAddPosition(currentState, nodes, currentState.player, x + 1, y, x + 2, y, Direction.RIGHT,
                            boxIndex);

                }
            } catch (OutOfMemoryError e) {
                // When memory is low, clear the history to free space and invoke garbage
                // collection.
                history.clear();
                System.gc();
                // Optionally log the event for debugging purposes.
            }

        }
        return null;
    }

    private static final Comparator<Position> comparePoints = new Comparator<Position>() {
        @Override
        public int compare(Position o1, Position o2) {
            if (o1.x == o2.x) {
                return o1.y - o2.y;
            } else {
                return o1.x - o2.x;
            }
        }
    };

    private static void testBoxAddPosition(State state, Queue<State> nodes, Position player, int boxX, int boxY,
            int boxX2, int boxY2, Direction move, int index) {
        if (state.isFree(boxX, boxY) && state.isFree(boxX2, boxY2)) {
            Position boxPos = state.boxes.get(index);
            Result result = Search.bfs(state, new IsAtPosition(move, boxPos.x, boxPos.y), player.x, player.y);
            if (result != null) {
                ArrayList<Position> boxes = new ArrayList<Position>(state.boxes);
                boxes.set(index, new Position(boxX, boxY));
                // Sort the boxes with a stable sort since we want that two configurations of
                // boxes which are permutations
                // of each other are equal
                Collections.sort(boxes, comparePoints);
                State possibleStep = new State(state.map, new Position(boxX2, boxY2), boxes, result.path, state);
                Collections.reverse(possibleStep.playerPath);
                possibleStep.playerPath.add(move);

                if (!inHistory(possibleStep)) {
                    nodes.add(possibleStep);
                }
            }
        }
    }

    public static ArrayList<Direction> getPlayerPath(State current) {
        if (current.parent == null) {
            return current.playerPath;
        } else {
            ArrayList<Direction> tempPath = getPlayerPath(current.parent);
            if (tempPath == null) {
                tempPath = new ArrayList<Direction>();
                tempPath.add(current.playerPath.get(current.playerPath.size() - 1));
            } else {
                tempPath.addAll(current.playerPath);
            }
            return tempPath;
        }
    }

    private static boolean inHistory(State state) {
        try {
            Position p = state.player;
            int x = p.x;
            int y = p.y;

            boolean[][] maybePlayerPositions = history.get(state.boxes);
            if (maybePlayerPositions == null) {
                maybePlayerPositions = new boolean[state.getWidth()][state.getHeight()];
            } else {
                if (maybePlayerPositions[x][y]) {
                    return true;
                }
            }
            floodfill(state, maybePlayerPositions, x, y, true);
            history.put(state.boxes, maybePlayerPositions);
            return false;
        } catch (OutOfMemoryError e) {
            // If an OutOfMemoryError occurs, clear history and force garbage collection
            history.clear();
            System.gc();
            return false;
        }
    }

    // Map which stores all player positions which has been observed for a specific
    // box configuration
    // The boxes are sorted first on the x-axis and then on they y-axis with a
    // stable sort so that it can be used as a key
    private static HashMap<ArrayList<Position>, boolean[][]> history = new HashMap<ArrayList<Position>, boolean[][]>();

}
