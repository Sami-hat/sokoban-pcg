// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

public class VisitedNodes
{
    public VisitedNodes(int width, int height)
    {
        directions = new Direction[width][height];
        tokens = new int[width][height];
    }

    public Direction get(int x, int y)
    {
        if (tokens[x][y] != currentToken)
        {
            return null;
        }
        return directions[x][y];
    }

    public void set(int x, int y, Direction dir)
    {
        tokens[x][y] = currentToken;
        directions[x][y] = dir;
    }

    static VisitedNodes createArray(int width, int height)
    {
        if (visitedNodes == null)
        {
            visitedNodes = new VisitedNodes(width, height);
        }
        visitedNodes.currentToken++;
        return visitedNodes;
    }
    private static VisitedNodes visitedNodes;

    public Direction[][] directions;
    private int tokens[][];
    private int currentToken;
}
