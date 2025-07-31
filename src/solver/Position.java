// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

public final class Position {
    public Position()
    {
        this.x = this.y = 0;
    }

    public Position(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Position(int[] pos)
    {
        this.x = pos[0];
        this.y = pos[1];
    }

    public Position(Position pos)
    {
        this.x = pos.x;
        this.y = pos.y;
    }

    public boolean equals(Object o)
    {
        return o.getClass() == this.getClass() && equals((Position)o);
    }

    public boolean equals(Position p)
    {
        return x == p.x && y == p.y;
    }

    public String toString()
    {
        return "{" + x + ", " + y + "}";
    }

    public int hashCode()
    {
        int hash = 17;
        hash = 31*hash + x;
        hash = 31*hash + y;
        return hash;
    }

    public int x, y;
}
