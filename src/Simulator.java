import solver.Direction;
import solver.Position;

import java.util.List;
import java.util.ArrayList;

class Box implements Comparable<Box> 
{
    /* Supporting class to uniquely identify and compare boxes on the grid */
    public Box(Point point, int object, Point goal) 
    { 
        this.point = point;
        this.id = object;
        this.goal = goal;
    }
    
    /* Find box based on point */
    public static Box getBox(Point point, List<Box> boxes)
    {
        for (Box b: boxes) 
        {
            if (b.getPoint().equals(point)) return b;
        }
        return null;
    }

    /* Getters */
    public Point getPoint() { return point; }
    
    public int getID() { return id; }
    
    public Point getGoal() { return goal; }
    
    /* Setters */
    public void setPoint(int x, int y) { this.point.setPoint(x, y); }
    
    public void setID(int object) { this.id = object; }
    
    public void setGoal(Point goal) { this.goal = goal; }
    
    @Override
    public int compareTo(Box other) 
    {
        /* Compare based on id */
        return Integer.compare(this.id, other.id);
    }

    private Point point;
    private int id;
    private Point goal;
    
}

public class Simulator 
{

    /* Constructor */
    public Simulator(Level level, Iterable<Direction> path, List<Point[]> pairs) 
    {
        this.level = level;
        this.path = path;
        this.pairs = pairs;

        this.totalMoves = size(path);

        copyGrid();
        storeEntites();
        simulate();
    }

    /* Copy the initial game map and player position to the current game map */
    private void copyGrid() 
    {
        
        int[][] gridI = level.getGrid();
        int rows = level.getRows();
        int cols = level.getCols();

        int[] playerI = level.getPlayer();
        int playerRowI = playerI[0];
        int playerColI = playerI[1];

        grid = new int[rows][cols];
        /* Set game map to initial game map */
        for (int x = 0; x < rows; x++) 
        {
            for (int y = 0; y < cols; y++) 
            {
                grid[x][y] = gridI[x][y];
            }
        }

        /* Set player position to initial position */
        playerRow = playerRowI;
        playerCol = playerColI;
    }

    /* Locate all boxes on the grid and store as entity objects */
    private void storeEntites() 
    {
        for (int i = 0; i < level.getEntites(); i++) 
        {
            Box entity = new Box(pairs.get(i)[1], i, pairs.get(i)[0]); 
            boxes.add(entity);
        }
    }

    /* Simulate moves, count pushes and box changes */
    public void simulate() 
    {
        boolean pushing = false;
        Direction prevStep = null;
        int prevBox = -1;

        for (Direction step : path) 
        {
            try 
            {
                Position dirPos = new Position();
                step.getDirection(dirPos);
    
                /* New player position */
                int newRow = playerRow - dirPos.y;
                int newCol = playerCol - dirPos.x;
    
                /* Get the tile at the new player position */
                int newPlayerTile = grid[newRow][newCol]; 
    
                if (newPlayerTile == Tile.WALL) continue;
                
                if (newPlayerTile == Tile.BOX || newPlayerTile == Tile.BOX_ON_GOAL) 
                {
                    /* If a box, check if new box position is not a wall or another box */
                    int newBoxRow = newRow - dirPos.y;
                    int newBoxCol = newCol - dirPos.x;
    
                    int newBoxTile = grid[newBoxRow][newBoxCol]; 
    
                    if (newBoxTile == Tile.WALL || newBoxTile == Tile.BOX || newBoxTile == Tile.BOX_ON_GOAL) continue;
                    else 
                    {
                        /* If so move box, either to goal or to free space */
                        grid[newBoxRow][newBoxCol] = (grid[newBoxRow][newBoxCol] == Tile.FLOOR) ? Tile.BOX : Tile.BOX_ON_GOAL;
                        /* Move player */
                        grid[newRow][newCol] = (grid[newRow][newCol] == Tile.BOX) ? Tile.PLAYER : Tile.PLAYER_ON_GOAL;

                        /* Get the box being pushed */
                        Box currBox = Box.getBox(new Point(newRow, newCol), boxes);

                        /// METRIC COUNTING ///
    
                        totalPushes++; /* Increment total pushes */
    
                        /* Start of a directional push */
                        if (!pushing) 
                        {
                            totalDirectionalPushes++; /* Increment total directional pushes */
                            pushing = true; 

                        } 
                        else 
                        {
                            /* If the previous step is not the same as the current step, it is a new push */
                            if (prevStep != step) totalDirectionalPushes++;
                        }
                        
                        /* Check if the push is in the opposite direction to its goal */
                        if (dirPos.x == 0) /* Moving vertically, compare y-axis position */
                        {
                            if 
                            (
                                (dirPos.y ==  1 && newBoxRow > currBox.getGoal().y) || /* Pushing Down */
                                (dirPos.y == -1 && newBoxRow < currBox.getGoal().y)    /* Pushing Up */
                            )
                            totalReversePushes++;
                        } 
                        else if (dirPos.y == 0) /* Moving horizontally, compare x-axis position */
                        {
                            if
                            (
                                (dirPos.x ==  1 && newBoxCol > currBox.getGoal().x) || /* Pushing Right */
                                (dirPos.x == -1 && newBoxCol < currBox.getGoal().x)    /* Pushing Left */
                            ) 
                            totalReversePushes++;
                        }
                        
                        /* Check if the box being pushed has changed */
                        currBox.setPoint(newBoxRow, newBoxCol); /* Update box position */
                        if (prevBox != currBox.getID()) /* If current box not the same as previous */
                        {
                            prevBox = currBox.getID(); /* Update ID of last box pushed */
                            totalBoxChanges++; /* Increment total box changes */
                        }
                    }
                } 
                else 
                {
                    /* Not a push, update player position */
                    pushing = false;
                    grid[newRow][newCol] = (newPlayerTile == Tile.FLOOR) ? Tile.PLAYER : Tile.PLAYER_ON_GOAL;
                }
    
                /* Update tile player moved from */ 
                grid[playerRow][playerCol] = (grid[playerRow][playerCol] == Tile.PLAYER) ? Tile.FLOOR : Tile.GOAL;
    
                playerRow = newRow;
                playerCol = newCol;
    
                prevStep = step;
            } 
            catch (ArrayIndexOutOfBoundsException e) 
            {
                System.out.println("Array Index Out of Bounds");
                System.out.println("Player Position: " + playerRow + ", " + playerCol);
                System.out.println("Direction: " + step);
                e.printStackTrace();
            }
        }
    }

    /* Get the size of the path */
    private int size(Iterable<Direction> path) 
    {
        int size = 0;
        for (@SuppressWarnings("unused") Direction dir : path) size++;
        return size;
    }

    /* Getters */
    public int getTotalMoves() { return totalMoves; }
    
    public int getTotalPushes() { return totalPushes; }
    
    public int getTotalDirectionalPushes() { return totalDirectionalPushes; }
    
    public int getTotalReversePushes() { return totalReversePushes; }
    
    public int getTotalBoxChanges() { return totalBoxChanges; }

    // Level Attributes
    private Level level;
    private Iterable<Direction> path;
    private List<Point[]> pairs;

    private int[][] grid;
    private int playerRow;
    private int playerCol;

    private List<Box> boxes = new ArrayList<>(); 
    
    // Metric Attributes
    private int totalMoves = 0;
    private int totalPushes = 0;
    private int totalDirectionalPushes = 0;
    private int totalReversePushes = 0;
    private int totalBoxChanges = 0;

}
