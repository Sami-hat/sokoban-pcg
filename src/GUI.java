import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI extends JFrame 
{
    public GUI() 
    {

        /* Load images */ 
        floorImg = new ImageIcon(Util.GRAPHICS_PATH + "floor_grey.png").getImage();
        wallImg = new ImageIcon(Util.GRAPHICS_PATH + "brick_wall.png").getImage();
        playerImg = new ImageIcon(Util.GRAPHICS_PATH + "player_idle.png").getImage();
        boxImg = new ImageIcon(Util.GRAPHICS_PATH + "box.png").getImage();
        goalImg = new ImageIcon(Util.GRAPHICS_PATH + "goal.png").getImage();
        boxGoalImg = new ImageIcon(Util.GRAPHICS_PATH + "box_on_goal.png").getImage();
        playerGoalImg = new ImageIcon(Util.GRAPHICS_PATH + "player_on_goal_idle.png").getImage();

        
        /* Menu screen */
        String levelSet = JOptionPane.showInputDialog(this, 
        "Controls:\n" +
        "- Arrow keys to move\n" +
        "- Space to reset level\n" +
        "- Comma to go to the next level\n"+
        "\n"+
        "Enter level set number (0 for default | -1 for random):\n");

        if (levelSet == null) System.exit(0);
        
        
        if (levelSet.equals("-1")) 
        {
            totalLevels = 0;
            generateLevel();
        }
        else 
        {
            totalLevels = Util.getTotalLines(levelSet);
            runLevel(levelIndex, levelSet);
        }

        /* Set up frame */ 
        setTitle("Sokoban");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* Create panel to draw game map */ 
        JPanel panel = new JPanel() 
        {
            @Override
            protected void paintComponent(Graphics g) 
            {
                super.paintComponent(g);
                /* Fill grid */ 
                g.setColor(Color.GRAY);
                g.fillRect(0, 0, cols * TILE_SIZE, rows * TILE_SIZE);
                /* Draw the tiles */ 
                for (int x = 0; x < rows; x++) 
                {
                    for (int y = 0; y < cols; y++) 
                    {
                        switch (grid[x][y]) 
                        {
                            case Tile.FLOOR:
                                g.drawImage(floorImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.WALL:
                                g.drawImage(wallImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.PLAYER:
                                g.drawImage(floorImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                g.drawImage(playerImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.BOX:
                                g.drawImage(boxImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.GOAL:
                                g.drawImage(floorImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                g.drawImage(goalImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.BOX_ON_GOAL:
                                g.drawImage(boxGoalImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                            case Tile.PLAYER_ON_GOAL:
                                g.drawImage(floorImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                g.drawImage(playerGoalImg, y*TILE_SIZE, x*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                                break;
                        }
                    }
                }
            }
        };

        panel.setPreferredSize(new Dimension(getPreferredSize()));
        add(panel);

        setFocusable(true);
        requestFocusInWindow();
        /* Add key listener */
        addKeyListener(new KeyAdapter() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) 
                {
                    movePlayer(-1, 0);
                } 
                else if (keyCode == KeyEvent.VK_DOWN) 
                {
                    movePlayer(1, 0);
                } 
                else if (keyCode == KeyEvent.VK_LEFT) 
                {
                    movePlayer(0, -1);
                }
                else if (keyCode == KeyEvent.VK_RIGHT) 
                {
                    movePlayer(0, 1);
                } 
                else if (keyCode == KeyEvent.VK_SPACE) 
                {
                    System.out.println("\nRESET LEVEL\n");
                    resetLevel();
                } 
                else if (keyCode == KeyEvent.VK_COMMA)
                { 
                    System.out.println("\nNEW LEVEL\n");
                    if (levelSet.equals("-1")) generateLevel();
                    else 
                    {
                        if (levelIndex == totalLevels-1) levelIndex = 0;
                        else  levelIndex++;
                        runLevel(levelIndex, levelSet);
                    }
                }

            }
        });
    }

    /* Copy the initial game map and player position to the current game map */
    private void copyGrid() 
    {
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

    /* Generate a new instance */
    private void generateLevel() 
    {
        level.generateLevel(-1);
        buildLevel();
    }

    /* Run an existing instance */
    private void runLevel(int levelIndex, String levelSet) 
    {
        /* Read seed from list of level seeds */
        String pathname = Util.getLevelSet(levelSet);
        long seed = Util.readLineFromFile(levelIndex, pathname);
        
        System.out.println(pathname + " " + seed);

        level.generateLevel(seed);
        buildLevel();

        /* Set index to next level */
        levelIndex++;
    }

    /* Build the instance */
    private void buildLevel() 
    {
        /* Get grid */
        gridI = level.getGrid();

        if(gridI == null) return;

        rows = gridI.length;
        cols = gridI[0].length;

        /* Get keeper positon */
        playerI = level.getPlayer();
        playerRowI = playerI[0];
        playerColI = playerI[1];
        
        /* Get entity count */
        entities = level.getEntites();

        System.out.println(level.printStats());
        System.out.println(level.getPath());

        copyGrid();
        repaint();

        getContentPane().setPreferredSize(getPreferredSize());
        pack();
        revalidate();

        /* Adjust the frame size to account for insets */
        Insets insets = getInsets();
        int frameWidth = cols * TILE_SIZE + insets.left + insets.right;
        int frameHeight = rows * TILE_SIZE + insets.top + insets.bottom;
        setSize(frameWidth, frameHeight);
    }

    /* Reset the level to its initial state */
    private void resetLevel() 
    {
        copyGrid();
        repaint();
    }

    /* Check if the given position is within the bounds of the game map */
    private boolean isInBounds(int x, int y) 
    {
        if (x >= 0 && x < rows && y >= 0 && y < cols) return true;
        return false;
    }

    /* Move the player in the given direction. If the player is next to a box, move the box as well */
    private void movePlayer(int dx, int dy) 
    {

        int newRow = playerRow + dx;
        int newCol = playerCol + dy;

        /* Check if new player position is within bounds and not a wall */
        if (!isInBounds(newRow, newCol)) return;
        
        /* Get the tile at the new player position */
        int newTile = grid[newRow][newCol]; 
        if (newTile == Tile.WALL) return;
        
        else if (newTile == Tile.FLOOR) 
        {
            grid[newRow][newCol] = Tile.PLAYER;
        } 
        else if (newTile == Tile.GOAL) 
        {
            grid[newRow][newCol] = Tile.PLAYER_ON_GOAL;
        } 
        else if (newTile == Tile.BOX || newTile == Tile.BOX_ON_GOAL) 
        {
            /* If a box, check if new box position is within bounds and not a wall or another box */
            int newBoxRow = newRow + dx;
            int newBoxCol = newCol + dy;
            if (!isInBounds(newBoxRow, newBoxCol)) return;

            int newBoxTile = grid[newBoxRow][newBoxCol]; 

            if (newBoxTile == Tile.WALL || newBoxTile == Tile.BOX || newBoxTile == Tile.BOX_ON_GOAL) return;
            else 
            {
                /* If so move box, either to goal or to free space */
                grid[newBoxRow][newBoxCol] = (grid[newBoxRow][newBoxCol] == Tile.FLOOR) ? Tile.BOX : Tile.BOX_ON_GOAL;
                /* Move player */
                grid[newRow][newCol] = (grid[newRow][newCol] == Tile.BOX) ? Tile.PLAYER : Tile.PLAYER_ON_GOAL;
            }
        }

        /* Set previous player position to floor or goal */ 
        grid[playerRow][playerCol] = (grid[playerRow][playerCol] == Tile.PLAYER) ? Tile.FLOOR : Tile.GOAL;
        playerRow = newRow;
        playerCol = newCol;

        /* Refresh */
        repaint();
        checkWin();
    }

    /* Have all the boxes been placed into goals */
    private void checkWin() 
    {
        if (countObject(Tile.BOX_ON_GOAL, grid) == entities) 
        {
            if (!hasWon) System.out.println("\nWIN\n");
            hasWon = true;
        } 
        else 
        {
            hasWon = false;
        }
    }

    /* Counts the number of an object on a map */
    private int countObject(int obj, int[][] map) 
    {
        int count = 0;
        for (int x = 0; x < map.length; x++) 
        {
            for (int y = 0; y < map[0].length; y++) 
            {
                if (map[x][y] == obj) count++;
            }
        }
        return count;
    }

    /* Resize frame to fit game map */
    @Override
    public Dimension getPreferredSize() 
    {
        return new Dimension(cols * TILE_SIZE, rows * TILE_SIZE);
    }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> 
        {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }

    // GUI Constants
    private static final int TILE_SIZE = 32;

    // Level Attributes
    private Level level = new Level();
    private int levelIndex;
    private int totalLevels;

    private int[][] gridI;
    private int[][] grid;

    private int rows; 
    private int cols;

    // Player Attributes
    private int[] playerI;

    private int playerRowI;
    private int playerColI;

    private int playerRow;
    private int playerCol;
    
    // Game State Attributes
    private int entities = 0;
    private boolean hasWon = false;
    
     // Graphic Attributes
     private Image floorImg;
     private Image wallImg; 
     private Image playerImg; 
     private Image boxImg;
     private Image goalImg; 
     private Image boxGoalImg; 
     private Image playerGoalImg; 

}