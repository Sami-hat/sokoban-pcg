// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;
import java.util.*;

// This class represents a undirected graph using adjacency list
// representation
class Graph
{
    private int V;   // No. of vertices

    // Array  of lists for Adjacency List Representation
    private LinkedList<Integer> adj[];
    int time = 0;
    static final int NIL = -1;

    // Constructor
    @SuppressWarnings({ "unchecked", "rawtypes" })Graph(int v)
    {
        V = v;
        adj = new LinkedList[v];
        for (int i=0; i<v; ++i)
            adj[i] = new LinkedList();
    }

    // Function to add an edge into the graph
    void addEdge(int v, int w)
    {
        adj[v].add(w);  // Add w to v's list.
        adj[w].add(v);    //Add v to w's list
    }

    // A recursive function that finds and prints bridges
    // using DFS traversal
    // u --> The vertex to be visited next
    // visited[] --> keeps track of visited vertices
    // disc[] --> Stores discovery times of visited vertices
    // parent[] --> Stores parent vertices in DFS tree
    void bridgeUtil(int u, boolean visited[], int disc[],
                    int low[], int parent[])
    {

        // Mark the current node as visited
        visited[u] = true;

        // Initialize discovery time and low value
        disc[u] = low[u] = ++time;

        // Go through all vertices adjacent to this
        Iterator<Integer> i = adj[u].iterator();
        while (i.hasNext())
        {
            int v = i.next();  // v is current adjacent of u

            // If v is not visited yet, then make it a child
            // of u in DFS tree and recur for it.
            // If v is not visited yet, then recur for it
            if (!visited[v])
            {
                parent[v] = u;
                bridgeUtil(v, visited, disc, low, parent);

                // Check if the subtree rooted with v has a
                // connection to one of the ancestors of u
                low[u]  = Math.min(low[u], low[v]);

                // If the lowest vertex reachable from subtree
                // under v is below u in DFS tree, then u-v is
                // a bridge
                if (low[v] > disc[u])
                    System.out.println(u+" "+v);
            }

            // Update low value of u for parent function calls.
            else if (v != parent[u])
                low[u]  = Math.min(low[u], disc[v]);
        }
    }


    // DFS based function to find all bridges. It uses recursive
    // function bridgeUtil()
    void bridge()
    {
        // Mark all the vertices as not visited
        boolean visited[] = new boolean[V];
        int disc[] = new int[V];
        int low[] = new int[V];
        int parent[] = new int[V];


        // Initialize parent and visited, and ap(articulation point)
        // arrays
        for (int i = 0; i < V; i++)
        {
            parent[i] = NIL;
            visited[i] = false;
        }

        // Call the recursive helper function to find Bridges
        // in DFS tree rooted with vertex 'i'
        for (int i = 0; i < V; i++)
            if (visited[i] == false)
                bridgeUtil(i, visited, disc, low, parent);
    }

    // Helper function to convert 2D grid coordinates to vertex index
    int getIndex(int row, int col, int rows, int cols) {
        return row * cols + col;
    }

    // Function to add edges between all tiles that are not equal to 1
    void arrayToGraph(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        // Iterate through the grid
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] != 1) {
                    // Add edge to the right neighbor if it's not 1
                    if (col + 1 < cols && grid[row][col + 1] != 1) {
                        addEdge(getIndex(row, col, rows, cols), getIndex(row, col + 1, rows, cols));
                    }
                    // Add edge to the bottom neighbor if it's not 1
                    if (row + 1 < rows && grid[row + 1][col] != 1) {
                        addEdge(getIndex(row, col, rows, cols), getIndex(row + 1, col, rows, cols));
                    }
                }
            }
        }
    }

    public static void main(String args[])
    {
        // Create graphs given in above diagrams
        int[][] grid = {
            {0, 0, 1, 0},
            {1, 0, 1, 0},
            {0, 0, 0, 1},
            {0, 1, 0, 0}
        };

        Graph graph = new Graph(grid.length * grid[0].length);
        graph.arrayToGraph(grid);
        graph.bridge();

        // System.out.println("Bridges in first graph ");
        // Graph g1 = new Graph(5);
        // g1.addEdge(1, 0);
        // g1.addEdge(0, 2);
        // g1.addEdge(2, 1);
        // g1.addEdge(0, 3);
        // g1.addEdge(3, 4);
        // g1.bridge();
        // System.out.println();

        // System.out.println("Bridges in Second graph");
        // Graph g2 = new Graph(4);
        // g2.addEdge(0, 1);
        // g2.addEdge(1, 2);
        // g2.addEdge(2, 3);
        // g2.bridge();
        // System.out.println();

        // System.out.println("Bridges in Third graph ");
        // Graph g3 = new Graph(7);
        // g3.addEdge(0, 1);
        // g3.addEdge(1, 2);
        // g3.addEdge(2, 0);
        // g3.addEdge(1, 3);
        // g3.addEdge(1, 4);
        // g3.addEdge(1, 6);
        // g3.addEdge(3, 5);
        // g3.addEdge(4, 5);
        // g3.bridge();
    }
}
// This code is contributed by Aakash Hasija
