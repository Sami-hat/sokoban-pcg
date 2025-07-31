// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

import java.util.ArrayList;
import java.util.Collections;

public class Solver {

    public static Iterable<Direction> solve(State state)
    {
        State boxToGoal;
        int px = state.player.x;
        int py = state.player.y;
        ArrayList<Position> possiblePlayerPositions = possibleStartPositions(state);
        for (Position player : possiblePlayerPositions)
        {
            boxToGoal = Search.findBoxPath(state, player.x, player.y);
            if (boxToGoal != null){
                Search.Result result = Search.bfs(boxToGoal, new IsAtPosition(px, py), boxToGoal.player.x, boxToGoal.player.y);
                if(result != null){
                    Collections.reverse(result.path);
                    ArrayList<Direction> toReturn = Search.getPlayerPath(boxToGoal);
                    toReturn.addAll(result.path);
                    Collections.reverse(toReturn);
                    return toReturn;
                }
                else
                {
                    if (SolverMain.DEBUG)
                    {
                        System.err.println("No path back");
                    }
                }
            }
            else
            {
                if (SolverMain.DEBUG)
                {
                    System.err.println("No solution" + state.playerEndPos);
                }
            }
        }
        return null;
    }

    /**
     * Since the game is played in reverse
     * @param state
     * @return
     */
    private static ArrayList<Position> possibleStartPositions(State state){
        ArrayList<Position> positions = new ArrayList<Position>();
        
        for(Position box : state.boxes){
            if(state.isFree(box.x+1,box.y)){
                positions.add(new Position(box.x+1,box.y));
            }
            if(state.isFree(box.x-1,box.y)){
                positions.add(new Position(box.x-1,box.y));
            }
            if(state.isFree(box.x,box.y+1)){
                positions.add(new Position(box.x,box.y+1));
            }
            if(state.isFree(box.x,box.y-1)){
                positions.add(new Position(box.x,box.y-1));
            }
        }
        return positions;
    }
}
