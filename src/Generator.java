import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import solver.Direction;
import solver.SolverMain;

public class Generator 
{
    /* Use a random seed or run a given seed */
    public Generator(long seed) 
    {
        this.seed = seed;

        random = (seed == -1 ? new Random() : new Random(this.seed));  
        time = -1;
        grid = null;

        generate();
    }
  
    /* Returns the grid */
    private void generate() 
    {
        long startTime = System.currentTimeMillis();
        do 
        {
            createGrid();
        } 
        /* 20 seconds time limit */
        while (!isSolvable() && (System.currentTimeMillis() - startTime) < TIMELIMIT); 
        
        if (grid == null) 
        {
            System.out.println("FAILED TO GENERATE GRID");
            System.exit(0);
        }
        else this.time = (int) (System.currentTimeMillis() - startTime);
    }

    /* Generate a random grid */
    private void createGrid() 
    {
        rows = (random.nextInt(MIN_DIV, MAX_DIV + 1) * DIV_SIZE);
        cols = (random.nextInt(MIN_DIV, MAX_DIV + 1) * DIV_SIZE);
        grid = new int[rows][cols];

        for (int[] row : grid) Arrays.fill(row, 1);
        placeTemplates();
    }

    /* Places a template in each division of the grid */
    private void placeTemplates() 
    {
        for (int x = 0; x < (rows / DIV_SIZE); x++) 
        {
            for (int y = 0; y < (cols / DIV_SIZE); y++) 
            {
                int[][] template = TEMPLATES[random.nextInt(TEMPLATES.length)];
                
                int rotations = random.nextInt(4);
                for (int r = 0; r < rotations; r++)
                {
                    template = rotateTemplate(template);
                } 
                /* Start a tile sooner to centre the 5x5 template in the 3x3 division */
                int startRow = (x == 0 ? -1 : (x * DIV_SIZE));
                int startCol = (y == 0 ? -1 : (y * DIV_SIZE));

                placeTemplate(template, startRow, startCol);
            }
        }
    }

    /* Array manipulation to rotate a template 90 degrees clockwise */
    private int[][] rotateTemplate(int[][] template) 
    {
        int n = template.length;
        int m = template[0].length;

        int[][] rotated = new int[m][n];
        for (int i = 0; i < n; i++) 
         {
            for (int j = 0; j < m; j++) 
            {
                rotated[j][n - 1 - i] = template[i][j];
            }
        }
        return rotated;
    }

    /* Place template on the grid */
    private void placeTemplate(int[][] template, int row, int col) 
    {
        int n = template.length;
        int m = template[0].length;
        
        for (int i = 0; i < n; i++) 
        {
            if (row + i < 0 || row + i >= rows) continue;
            for (int j = 0; j < m; j++) 
            {
                if (col + j < 0 || col + j >= cols) continue;
                if (template[i][j] != -1) grid[row + i][col + j] = template[i][j];
            }
        }
    }
    
    /* BFS, checks if all floor tiles are connected */
    private boolean bfs() 
    {
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        
        /* Find the first floor tile to start BFS */ 
        int[] floorTile = getEntity(Tile.FLOOR);
        queue.offer(floorTile);
        visited[floorTile[0]][floorTile[1]] = true;
        
        /* BFS */
        while (!queue.isEmpty()) 
        {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            
            for (int[] dir : DIRECTIONS) 
            {
                int newX = x + dir[0];
                int newY = y + dir[1];
                
                /* Check if the new coordinates are valid */
                if (isInBounds(newX, newY, rows, cols) && !visited[newX][newY] && grid[newX][newY] == Tile.FLOOR) 
                {
                    queue.offer(new int[]{newX, newY});
                    visited[newX][newY] = true;
                }
            }
        }
        
        /* Check if all floor tiles are visited */
        for (int row = 0; row < rows; row++) 
        {
            for (int col = 0; col < cols; col++) 
            {
                if (grid[row][col] == Tile.FLOOR && !visited[row][col]) 
                    return false;
            }
        }
        return true;
    }
    
    /* Check if the given coordinates are within the grid */
    private boolean isInBounds(int x, int y, int row, int col) { return x >= 0 && x < row && y >= 0 && y < col; }

    /* Check if grid is fully connected, place entites, check solvability*/
    private boolean isSolvable() 
    {
        /* Start again if BFS fails */
        if (!bfs()) return false;
        borderGrid();

        /* Place entities on grid */
        EntityGenerator entityGen;
        entities = (random.nextInt(MIN_ENTITIES, MAX_ENTITIES + 1));
        entityGen = new EntityGenerator(grid, entities, random);
        grid = entityGen.getGrid();

        /* Restart on invalid grids */
        if (grid == null) return false; 

        /* Write grid to file, run solver */
        String pathname = Util.writeToFile(seed, grid);
        try 
        {
            /* Solve instance */
            path = SolverMain.solvePuzzle(pathname);
            while (path == null) 
            {
                /* Attempt to re-place keeper if no solution */
                if (!entityGen.replaceKeeper()) return false; /* No solution, if can't place keeper */
                grid = entityGen.getGrid(); /* Get new grid */
                pathname = Util.writeToFile(seed, grid);
                path = SolverMain.solvePuzzle(pathname); /* Resolve instance */
            }

            /* Simulate moves */
            Simulator sim = new Simulator(new Level(grid, getPlayer(), entities), path, entityGen.getPairs());

            String sokobanContent = Util.convertToSokobanFormat(grid);
            try (java.io.PrintWriter out = new java.io.PrintWriter("levels/JSoko.txt")) 
            {
                out.print(sokobanContent);
            } 
            catch (java.io.IOException e) 
            {
                e.printStackTrace();
            }

            /* Get metrics */
            moves = sim.getTotalMoves();
            pushes = sim.getTotalPushes();
            directionalPushes = sim.getTotalDirectionalPushes();
            reversePushes = sim.getTotalReversePushes();
            boxChanges = sim.getTotalBoxChanges() - 1;
            cornerGoals = entityGen.getCornerGoals();

            return true;
        } 
        catch (Exception error) 
        {
            error.printStackTrace();
            return false;
        }
        catch (OutOfMemoryError error) 
        {
            System.out.println("Heap space exceeded");
            error.printStackTrace();
            return false;
        }
    }
    
    /* Border the grid with walls */
    private void borderGrid() 
    {
        int bRows = rows + 2;
        int bCols = cols + 2;
        int[][] borderedGrid = new int[bRows][bCols];
        for (int x = 0; x < bRows; x++) 
        {
            for (int y = 0; y < bCols; y++) 
            {
                if (x == 0 || x == rows + 1 || y == 0 || y == cols + 1) 
                    borderedGrid[x][y] = Tile.WALL;
                else 
                    borderedGrid[x][y] = grid[x - 1][y - 1];
            }
        }
        rows = bRows; cols = bCols;
        grid = borderedGrid;
    }

    /* Testing functions to fill sections of grid */
    @SuppressWarnings("unused")
    private void fillRows(int rowIndex) 
    { 
        for (int i = rowIndex; i < rows; i++)
            Arrays.fill(grid[i], 1);
    }
    @SuppressWarnings("unused")    
    private void fillColumns(int colIndex) 
    {
        for (int i = 0; i < rows; i++) 
        {
            for (int j = colIndex; j < cols; j++)
                grid[i][j] = 1;
        }
    }

    /* Score level */
    private void scoreLevel() 
    {
        /* Calculate fEmp and fDiv */
        TwoArch2 twoArch = new TwoArch2(grid);

        int fEmp = twoArch.getfEmp();
        double fDiv = twoArch.getfDiv();

        /* Calculate teritary metrics */
        double box_grid_ratio = Math.sqrt((rows-2) * (cols-2));
        int totalTiles = (rows - 2) * (cols - 2); /* Account for borders */
        double maxMoves = Math.sqrt(Math.pow(totalTiles,2) * entities); 

        
        /* Retrieve metrics */
        int[] metrics = getMetrics();
        
        /* Score metrics */
        double M = metrics[0];                 /* Moves */
        double P = metrics[1];                 /* Pushes */
        double D = metrics[2];                 /* Directional Pushes */
        double I = metrics[3];                 /* Reverse Pushes */
        double C = metrics[4];                 /* Box Changes */
        double G = metrics[5];                 /* Corner Goals */
        double B = metrics[6];                 /* Boxes */
        double R;                              /* Random */
        
        if (M == -1) System.exit(0);
        
        /* Score normalisation */ 
        C = (C - 1) / (P - 1);                 /* Box Changes as % of the total pushes */
        D = (D - 1) / (P - 1);                 /* Directional Pushes as % of the total pushes */
        I = (I - 0) / (P - 0);                 /* Reverse Pushes as % of the total pushes */
        P = (P - 1) / (M - 1);                 /* Pushes as % of the total moves */
        G = (G - 0) / (B - 0);                 /* Corner Goals as % of the total goals */
        B = (B / box_grid_ratio);              /* Boxes as % of the total grid, borderless */
        R = (0.05 * Math.random());            /* Random float between 0.00 and 0.05 */
        

        /* Calculate bounds of score */
        double maxRating = (Math.max(maxMoves, M) * a) + (1 * b) + (1 * c) + (1 * d) + (1 * e) - (0 * f) - ((entities/box_grid_ratio) * g) + 0.05;
        double minRating = (0 * a) + (0 * b) + (0 * c) + (0 * d) + (0 * e) - (entities * f) - ((entities/box_grid_ratio) * g) + 0.00;


        /* Calculate composite rating */
        rating = (a * M) + (b * P) + (c * D) + (d * I) + (e * C) - (f * G) - (g * B) + (R);
        rating = (rating - minRating) / (maxRating - minRating);


        String resultsFile = "results/9x9_6B/composite_new.csv";
        java.io.File file = new java.io.File(resultsFile);
        if (!file.exists() || file.length() == 0) 
        {
            try (BufferedWriter headerWriter = new BufferedWriter(new FileWriter(file, true))) 
            {
                headerWriter.write("moves,pushes,dir_pushes,rev_pushes,box_changes,corner_goals,fDiv,fEmp,rating,seed");
                headerWriter.newLine();
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
            }
        }

        try (BufferedWriter r = new BufferedWriter(new FileWriter(resultsFile, true))) 
        {
            r.write(printStatsCSV() + ","+ fDiv + "," + fEmp + "," + printLevelIdCSV() + "\n");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        Util.clearAll();
    }

    /* Print stats for CSV */
    public String printStatsCSV() 
    { 
        int[] metrics = getMetrics();
        return metrics[0] + "," +
               metrics[1] + "," +
               metrics[2] + "," +
               metrics[3] + "," +
               metrics[4] + "," +
               metrics[5];
    }
    
    /* Print level ID for CSV */
    public String printLevelIdCSV() 
    { 
        return rating + "," + seed;
    }

    /* Print stats */
    public String printStats() 
    { 
        int[] metrics = getMetrics();
        return
        (
            "MOVES: " + metrics[0] + " | PUSHES: "  + metrics[1] + 
            " | DIR PUSHES: " + metrics[2] +  " | REV PUSHES: " + metrics[3] +
            " | BOX CHANGES: " + metrics[4] + " | CORNER GOALS: " + metrics[5]
        ); 
    }

    /* Print level ID */
    public String printLevelId() 
    { 
        return "RATING: " + rating + " | SEED: " + seed;
    }
    
    /* Print grid */
    public void printGrid() 
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

    /* Getters */
    public int[] getMetrics() { return new int[]{this.moves, this.pushes, this.directionalPushes, 
                                                 this.reversePushes, this.boxChanges, this.cornerGoals, this.entities}; }
    
    public Iterable<Direction> getPath() { return this.path; } 
    
    public int getEntities() { return this.entities; }
    
    public int[] getEntity(int entity) 
    {
        for (int x = 0; x < rows; x++) 
        {
            for (int y = 0; y < cols; y++) 
            {
                if (grid[x][y] == entity) 
                return new int[]{x, y};
            }
        }
        return null;
    }
    
    public int[] getPlayer() 
    { 
        int[] player = getEntity(Tile.PLAYER);
        if (player == null) return getEntity(Tile.PLAYER_ON_GOAL);
        return player;
    }

    public int[][] getGrid() { return this.grid; }

    public long getSeed() { return this.seed; }
    
    public static void main(String[] args) 
    {
        long seed = System.currentTimeMillis();
        Generator generator = new Generator(seed);

        System.out.println("Duration: " + generator.time + "ms");
        System.out.println("Seed: " + generator.getSeed());
        generator.printGrid();
        generator.scoreLevel();
    }

    // Division Constants
    private static final int MIN_DIV = 3;
    private static final int MAX_DIV = MIN_DIV; /* Set equal for testing conditions */
    
    // Template Constants
    private static final int[][][] TEMPLATES = Templates.TEMPLATES;
    private static final int DIV_SIZE = 3;
    
    // Entity Constants
    private static final int MIN_ENTITIES = 6;
    private static final int MAX_ENTITIES = MIN_ENTITIES; /* Set equal for testing conditions */

    // Generation Constants 
    private static final int TIMELIMIT = 600000; /* 10 minutes in milliseconds */
    private int time = -1; 
    
    // Search Constants
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    // RNG
    private long seed;
    private Random random;

    // Grid Attributes
    private int[][] grid;
    private int rows;
    private int cols;

    // Entity Attributes
    private int entities;

    // Solver Attributes
    private Iterable<Direction> path = null;

    private int moves = -1;
    private int pushes = -1;
    private int directionalPushes = -1;
    private int reversePushes = -1;
    private int boxChanges = -1;
    private int cornerGoals = -1;
    private double rating = -1;

    private static final double a = 0.0005;
    private static final double b = 0.35;
    private static final double c = 0.5;
    private static final double d = 0.3;
    private static final double e = 2.0;
    private static final double f = 0.3;
    private static final double g = 0.4;
}