import java.util.ArrayList;

import solver.Direction;

/* Bridge class between GUI and Generation */
public class Level
{
    /* Constructor */
    public Level(int[][] grid, int[] player, int entities) 
    {
        this.grid = grid;
        rows = grid.length;
        cols = grid[0].length;

        this.player = player;
        this.entities = entities;
    }

    /* Testing Constructor */
    public Level () {}

    /* Generate a level or reconstruct a level given a seed */
    public void generateLevel(long seed)
    {
        Generator generator = new Generator(seed);

        grid = generator.getGrid();
        if (grid == null) return;

        rows = grid.length;
        cols = grid[0].length;

        player = generator.getPlayer();
        entities = generator.getEntities();
        path = rle(generator.getPath());

        metrics = generator.getMetrics();
    }

    /* Run-length encoding of path */
    public String rle(Iterable<Direction> path) 
    {
        if (path == null) return null;
        ArrayList<String> encodedPath = new ArrayList<>();
        for (Direction step : path) 
        {
            encodedPath.add(step.toString());
        }

        String encodedString = "";
        for (int i = 0, count = 1; i < encodedPath.size(); i++) {
            if (i + 1 < encodedPath.size() && encodedPath.get(i) == encodedPath.get(i + 1))
                count++;
            else {
                encodedString = encodedString
                        .concat(Integer.toString(count))
                        .concat((encodedPath.get(i)))
                        .concat(" ");
                count = 1;
            }
        }
        return encodedString;
    
        
    }
    
    /* Getters */
    public int[][] getGrid() { return grid; }
    
    public int getRows() { return rows; }
    
    public int getCols() { return cols; }
    
    public int[] getPlayer() { return player; }
    
    public int getEntites() { return entities; }
    
    public String getPath() { return path; }
    
    /* Print stats */
    public String printStats(int[] metrics) 
    { 
        return 
        (
            "MOVES: " + metrics[0] + " | PUSHES: "  + metrics[1] + 
            " | DIR PUSHES: " + metrics[2] +  " | REV PUSHES: " + metrics[3] +
            " | BOX CHANGES: " + metrics[4] + " | CORNER GOALS: " + metrics[5]
        ); 
    }
    public String printStats() 
    { 
        return 
        (
            "MOVES: " + metrics[0] + " | PUSHES: "  + metrics[1] + 
            " | DIR PUSHES: " + metrics[2] +  " | REV PUSHES: " + metrics[3] +
            " | BOX CHANGES: " + metrics[4] + " | CORNER GOALS: " + metrics[5]
        ); 
    }

    /* Print grid */
    public static void printGrid(int[][] grid) 
    {
        System.out.println("GRID: ");
        for (int[] row : grid) 
        {
            for (int cell : row) 
            {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println("\n");
    }
    
    // Level Attributes
    private int[][] grid;
    private int rows;
    private int cols;

    private int[] player;
    private int entities;
    
    // Solver Attributes
    private String path;
    private int[] metrics;
    
}
