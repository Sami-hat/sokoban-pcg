// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

public interface SearchTest {

    /**
     * Interface to be used when determining the end of a search
     * @param state The state being searched
     * @param x Position
     * @param y Position
     * @return True if the tile is the searched for tile
     */
    public boolean isEnd(State state, int x, int y);
}
