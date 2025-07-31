// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;
import java.io.*;
import java.util.ArrayList;

public class SolverMain {

    public static final boolean DEBUG = false;

    public static ArrayList<String> loadBoard(String filename) throws IOException {
        ArrayList<String> board = new ArrayList<String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while (br.ready()) {
            line = br.readLine();
            board.add(line);
        }

        if (board.isEmpty()) {
            br = new BufferedReader(new FileReader(filename));

            while (br.ready()) {
                line = br.readLine();
                board.add(line);
            }
        }

        return board;
    }

    public static Iterable<Direction> solvePuzzle(String filename) throws IOException, Exception {
        ArrayList<String> board = loadBoard(filename);

        Map map = new Map(board);
        Map invertMap = Map.inverted(map);
        if (DEBUG) {
            System.err.println(map.toString());
        }
        State initialState = new State(invertMap, map.inverseMap, board);
        
        Iterable<Direction> path = Solver.solve(initialState);
        return path;
    }

    public static void main(String[] args) throws IOException, Exception {
        if (args.length > 0) {
            solvePuzzle(args[0]);
        } else {
            System.out.println("Please provide a filename as an argument.");
        }
    }
}
