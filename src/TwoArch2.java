public class TwoArch2 
{
    /* Constructor */
    public TwoArch2(int[][] grid) 
    {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;

        fEmptiness(); /* fEmp: Proportion of empty spaces in the level */
        fDiversity(); /* fDiv: Diversity of empty spaces in the level */
    }

    /* Calculate fEmp */
    private void fEmptiness() 
    {
        int totalFloor = 0;
        
        for (int i = 1; i < rows-1; i++) 
        {
            for (int j = 1; j < cols-1; j++) 
            {
                if (grid[i][j] == Tile.FLOOR) 
                    totalFloor++;
            }
        }
        
        fEmp = totalFloor;
    }
    
    /* Calculate fDiv */
    private void fDiversity() 
    {   
        double[] proportions = new double[rows-2]; /* Proportion of empty spaces per row, ignore borders */
        
        /* Calculate p_i (proportions of empty spaces per row) */
        for (int i = 1; i < rows-1; i++) 
        {
            int emptyCount = 0;
            for (int j = 1; j < cols-1; j++) 
            {
            if (grid[i][j] == Tile.FLOOR)
                emptyCount++;
            }
            proportions[i-1] = (double) (emptyCount) / (cols-2);
        }

        /* Normalise the proportions: ∑(α * p_i) = 1 */ 
        double sumProportions = 0;
        
        for (double p : proportions) 
        sumProportions += p;
        
        double alpha = 1.0 / sumProportions;

        /* Compute fDiv */ 
        fDiv = 0.0;
        for (double p : proportions) 
        {
            double normalizedP = (alpha * p) / (rows-2);
            if (normalizedP > 0)  /* Avoid log(0) error */ 
            fDiv += normalizedP * Math.log(normalizedP);
        }
        
        fDiv = fDiv * (-1 / Math.log(rows-2));

        /* Normalise again based on rows */   
        fDiv = fDiv * ((rows-2)/2);
    }
    
    /* Getters */
    public int getfEmp() { return fEmp; }
    
    public double getfDiv() { return fDiv; }
    
    /* Comparetor */
    public int comparePareto(TwoArch2 other)
    {
        /* Current has a better score than the Next */
        if (this.fEmp >= other.fEmp && this.fDiv >= other.fDiv) return 1;
        /* Current has an equal score to the Next */
        if (this.fEmp == other.fEmp && this.fDiv == other.fDiv) return 0;
        /* Current has a lower score than the Next */
        return -1;
    }

    // Level Attributes
    private int[][] grid;
    private int rows;
    private int cols;
    
    // Metric Attributes
    private int fEmp;
    private double fDiv;
}
