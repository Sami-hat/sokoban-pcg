// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

public class IsAtPosition implements SearchTest {

    /**
     *
     * @param direction Is it a box in this direction?
     */
    public IsAtPosition(Direction direction, int x, int y)
    {
        direction.getDirection(position);
        position.x = x + position.x;
        position.y = y + position.y;
    }

    public IsAtPosition(int x, int y)
    {
        position.x = x;
        position.y = y;
    }

    @Override
    public boolean isEnd(State state, int x, int y) {
        return x == position.x && y == position.y;
    }

    private final Position position = new Position();
}
