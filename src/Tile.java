import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* Tile class to represent composition of the grid */
public class Tile 
{
    // Tile Type Constants
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int PLAYER = 2;
    public static final int BOX = 3;
    public static final int GOAL = 4;
    public static final int BOX_ON_GOAL = 5;
    public static final int PLAYER_ON_GOAL = 6;

    public static final Set<Integer> BLOCKADES = new HashSet<Integer>(Arrays.asList(WALL, BOX, BOX_ON_GOAL));
    private static final Set<Integer> ENTITIES = new HashSet<Integer>(Arrays.asList(BOX, GOAL, BOX_ON_GOAL));
    private static final Set<Integer> PASSWAY = new HashSet<Integer>(Arrays.asList(FLOOR, GOAL, PLAYER, PLAYER_ON_GOAL));

    public static boolean isBlockade(int tile) { return BLOCKADES.contains(tile); }
    public static boolean isEntity(int tile) { return ENTITIES.contains(tile); }
    public static boolean isPassway(int tile) { return PASSWAY.contains(tile); }
}
