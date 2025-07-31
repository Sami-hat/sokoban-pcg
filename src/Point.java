public class Point implements Comparable<Point> 
{
    /* Point class to represent coordinates on the grid */
    public Point(int x, int y) 
    {
        this.x = x;
        this.y = y;
        this.farthestState = 0;
    }
    
    public Point() 
    {
        this(-1, -1);
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (this == obj)
        return true;
        if (obj == null || getClass() != obj.getClass())
        return false;
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }
    
    public void setPoint(int x, int y) { this.x = x; this.y = y; }
    
    @Override
    public int hashCode() { return 31 * x + y; }
    
    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
    
    @Override
    public int compareTo(Point other) { return Integer.compare(manhattanDistance(this, other), 0); }
    
    /* Find manhattan distance between two points */
    public static int manhattanDistance(Point a, Point b) { return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); }
    
    /* Find manhattan 'product' between two points */
    public static int manhattanProduct(Point a, Point b) { return Math.abs(a.x - b.x) * Math.abs(a.y - b.y); }
    
    /* Check nodes between points for a blockade */
    public static boolean containsBlockade(Point a, Point b, int[][] grid) 
    {
        for (int i = Math.min(a.x, b.x); i <= Math.max(a.x, b.x); i++) 
        {
            for (int j = Math.min(a.y, b.y); j <= Math.max(a.y, b.y); j++) 
            {
                if (new Point(i, j).equals(a) || new Point(i, j).equals(b)) continue;
                if (Tile.isBlockade(grid[i][j])) return true;
            }
        }
        return false;
    }

    int x, y, farthestState;
    
}