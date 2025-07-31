import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;

public class EntityGenerator 
{
    /* Constructor */
    public EntityGenerator(int[][] grid, int entities, Random random) 
    {
        this.grid = grid;
        rows = grid.length;
        cols = grid[0].length;

        this.entities = entities;

        this.random = (random == null) ? new Random() : random;

        markPassway();
        markGoalPoints();
        placeEntities();
    }

    /* Mark all floor tiles as passable paths */
    private void markPassway() 
    {   
        passway = new ArrayList<Point>();
        for (int i = 0; i < rows; i++) 
        {
            for (int j = 0; j < cols; j++) 
            {
                Point cell = new Point(i, j);
                if (Tile.isPassway(grid[i][j])) passway.add(cell);
            }
        }
    }

    /* Mark tiles on passway that are reachable as goals */
    private void markGoalPoints() 
    {
        for (Point cell : passway) 
        {
            if (isReachable(cell)) goalPoints.add(cell);
        }
    }

    /* Mark subset of goal areas that are reachable as the new goal areas */
    private void markNewGoalPoints() 
    {
        if (goalPoints.isEmpty()) return;

        List<Point> newGoalAreas = new ArrayList<Point>();
        for (Point cell : goalPoints) 
        {
            if (isReachable(cell)) newGoalAreas.add(cell);
        }
        boxPoints.clear();
        goalPoints.clear();
        goalPoints.addAll(newGoalAreas);
    }
    

    /* Check if out of bounds, wall, or box */
    private boolean isBlockade(int x, int y) { return (x < 0 || y < 0 || x >= rows || y >= cols || Tile.isBlockade(grid[x][y])); }
    private boolean isBlockade(Point cell) { return isBlockade(cell.x, cell.y); }
    
    /* Corner requires two adjacent cells at 90° to be walls */
    private int isCorner(Point cell) 
    {
        int corners = 0;
        if (isBlockade(cell)) return 4;
        for (int[][] corner : CORNERS) 
        {
            if (
                isBlockade(cell.x + corner[0][0], cell.y + corner[0][1]) &&
                isBlockade(cell.x + corner[1][0], cell.y + corner[1][1])
               )  
                corners++;
        }
        return corners;
    }

    /* A goal is reachable if there is space for both the keeper and the box in one direction */
    private boolean isReachable(Point cell) 
    {
        int count = 0;
        for (int[] dir : DIRECTIONS) 
        {
            /* If two spaces out in a direction is accessilbe, and hasn't been covered, add to access point list */
            if (
                !isBlockade(cell.x + dir[0], cell.y + dir[1]) &&
                !isBlockade(cell.x + dir[0] * 2, cell.y + dir[1] * 2)
               )
                count++;
        }
        return count > 0;
    }

    /* Find all directions a goal can be accessed from */
    private ArrayList<Point> findAccessNeighbours(Point cell) 
    {
        ArrayList<Point> accessPoints = new ArrayList<Point>();
        for (int[] dir : DIRECTIONS) 
        {
            Point accessPoint = new Point(cell.x + dir[0], cell.y + dir[1]);
            Point outerPoint = new Point(cell.x + dir[0] * 2, cell.y + dir[1] * 2);

            /* If reachable, and hasn't been covered, add to access points */
            if (
                !isBlockade(accessPoint) &&
                isCorner(outerPoint) == 0 &&
                !boxPoints.contains(accessPoint)
               ) 
                /* Add to immediate access points of that cell to list */
                accessPoints.add(accessPoint);
        }

        if (accessPoints.isEmpty()) return null;
        return accessPoints;
    }

    /* Find all valid areas to place a box for each goal, BFS */
    private void findAccessRange(Point cell) 
    {
        Queue<Point> queue = new LinkedList<Point>();
        queue.add(cell);

        while (!queue.isEmpty() && queue.size() < passway.size()) 
        {
            Point current = queue.poll();
            ArrayList<Point> accessPoints = findAccessNeighbours(current);

            if (accessPoints != null) 
            {
                /* If access points found, add to queue and to box points */
                for (Point accessPoint : accessPoints) 
                {
                    boxPoints.add(accessPoint);
                    queue.add(accessPoint);
                }
            }
        }
    }


    /* Randomly select a goal location and find the available areas for a box to be placed */
    private void filterGoalRange() 
    {
        int index = random.nextInt(goalPoints.size());
        Point goalPoint = goalPoints.get(index);

        findAccessRange(goalPoint);

        /* Remove all points that are too close to the goal */
        boxPoints.removeIf(point -> Point.manhattanDistance(goalPoint, point) < MIN_MANHATTAN);

        /* Remove all points that are on a direct path from the goal if no blockade between */
        boxPoints.removeIf(point -> Point.manhattanProduct(goalPoint, point) == 0 && !Point.containsBlockade(goalPoint, point, grid));

        /* Find the farthest position a box is from any goal */
        for (Point bp : boxPoints) 
        {
            for (Point gp : goalPoints) 
            {
                if (Point.manhattanDistance(bp, gp) > bp.farthestState) 
                    bp.farthestState = Point.manhattanDistance(bp, gp);
            }
        }

        /* Sort box points by farthest state */
        boxPoints.sort((p1, p2) -> Integer.compare(p2.farthestState, p1.farthestState));

        /* Remove all points that are too close to any goal */
        boxPoints.removeIf(point -> point.farthestState < MIN_MANHATTAN);

        /* Remove all points that have no overlap with other goal ranges */
        if (!totalBoxPoints.isEmpty()) boxPoints.retainAll(totalBoxPoints); 

        /* If there is an available spot for a box corressponding to this goal, place both */
        if (!boxPoints.isEmpty()) placeBoxGoalPair(goalPoint);
        /* Else, remove goal from the potential goal positions and try again */
        else 
        {
            goalPoints.remove(goalPoint);
            if (goalPoints.isEmpty()) 
            {
                grid = null;
                return;
            }
            filterGoalRange();  
        }
    }

    /* Place the goal and box pair */
    private void placeBoxGoalPair(Point goalPoint) 
    {   
        /* Select first index from goal range, the furthest point */
        int limit = boxPoints.size() >= 3 ? 3 : boxPoints.size();
        Point boxPoint = boxPoints.get(random.nextInt(limit));

        /* Place goal */
        grid[goalPoint.x][goalPoint.y] = (grid[goalPoint.x][goalPoint.y] == Tile.FLOOR) ? Tile.GOAL : Tile.BOX_ON_GOAL;
        /* Place box */
        grid[boxPoint.x][boxPoint.y] = (grid[boxPoint.x][boxPoint.y] == Tile.FLOOR) ? Tile.BOX : Tile.BOX_ON_GOAL;

        if (isCorner(goalPoint) > 0) cornerGoals++; /* Increment corner goals */

        goals.add(goalPoint);
        boxes.add(boxPoint);
        pairs.add(new Point[] { goalPoint, boxPoint });

        /* Add to total access range for overlapping path filtering */
        totalBoxPoints.addAll(boxPoints);
        
    }

    /* Place all pairs of goals and boxes */
    private void placeEntities() 
    {
        int entity = 0;
        while (grid != null && entity < entities) 
        {
            /* If goal range is empty, invalid generation */
            if (goalPoints.isEmpty()) break;
            /* Filter available goal locations and place goal and box pair */
            filterGoalRange(); 
            /* Clear and remark */
            markNewGoalPoints();

            entity++;
        }
        /* If all objects placed, place keeper */
        if (goals.size() != boxes.size()) grid = null;
        if (grid == null) return; 
        segmentGrid();
        sortKeeperPositions();
        placeKeeper();
        /* If keeper is null, invalid generation */
        if (keeper == null) 
        {
            grid = null;
            return;
        }
    }

    /* Place Keeper in a random passway cell */
    private void placeKeeper() 
    {
        if (keeperPoints.isEmpty()) return;
        /* Select a random point from the list of keeper points */
        keeper = keeperPoints.get(random.nextInt(keeperPoints.size()));
        grid[keeper.x][keeper.y] = (grid[keeper.x][keeper.y] == Tile.FLOOR) ? Tile.PLAYER : Tile.PLAYER_ON_GOAL;
    }

    /* Re-place keeper after if previous segment results in deadlock */
    public boolean replaceKeeper()
    {
        keeperPoints.remove(keeper);
        if (keeperPoints.isEmpty()) return false;
        else placeKeeper();
        return true;
    }

    /* Sort keeper positions based on number of nearby walls */
    private void sortKeeperPositions() 
    {
        keeperPoints.sort((p1, p2) -> Integer.compare(isCorner(p1), isCorner(p2)));
    }

    /* Segment the grid into accessible regions for the keeper */
    private void segmentGrid() 
    {
        markPassway();
        boolean[][] visited = new boolean[rows][cols];
        for (Point tile : passway) 
        {
            /* If the cell is a floor and hasn't been visited, it's a new region */
            if (!visited[tile.x][tile.y]) 
            {
                /* Record the representative point for this region */
                keeperPoints.add(tile);

                /* Begin BFS to mark all cells in this region */ 
                Queue<Point> queue = new LinkedList<>();
                queue.offer(tile);
                visited[tile.x][tile.y] = true;

                while (!queue.isEmpty()) 
                {
                    Point point = queue.poll();

                    // Check neighbors
                    for (int[] dir: DIRECTIONS) 
                    {
                        int newX = point.x + dir[0];
                        int newY = point.y + dir[1];
                        if (!isBlockade(newX, newY) && !visited[newX][newY]) 
                        {
                            queue.offer(new Point(newX, newY));
                            visited[newX][newY] = true;
                        }
                    }
                }
            }
        }
    }

    /* Return grid */ 
    public int[][] getGrid() { return grid; }

    /* Get corner goal count */
    public int getCornerGoals() { return cornerGoals; }

    /* Get box goal pairs */
    public List<Point[]> getPairs() { return pairs; }

    // Entity Constants
    private static final int MIN_MANHATTAN = 3;
    
    // Search Constants
    private static final int[][] DIRECTIONS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
    private static final int[][][] CORNERS = { 
                                              { { -1, 0 }, { 0, -1 } }, { { -1, 0 }, { 0, 1 } },
                                              { {  1, 0 }, { 0, -1 } }, { {  1, 0 }, { 0, 1 } } 
                                             };

    // RNG
    private Random random;

    // Grid Attributes
    private int[][] grid;
    private int rows;
    private int cols;

    // Entity Attributes
    private int entities;

    // Metric Attributes
    private int cornerGoals = 0; /* Number of corner goals */

    // Point Lists
    private List<Point> passway = new ArrayList<Point>();;

    private List<Point> goalPoints = new ArrayList<Point>();;
    private List<Point> boxPoints = new ArrayList<Point>();;
    private List<Point> keeperPoints = new ArrayList<Point>();;

    private List<Point> totalBoxPoints = new ArrayList<Point>();;

    // Final Entity Lists
    private Set<Point> goals = new HashSet<Point>();
    private Set<Point> boxes = new HashSet<Point>();
    private List<Point[]> pairs = new ArrayList<Point[]>();;
    private Point keeper = null;

}
