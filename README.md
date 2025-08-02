# Sokoban Procedural Content Generation

A procedural content generation (PCG) tool for creating Sokoban puzzles, developed as my dissertation project exploring stochastic approaches to generating content for PSPACE-complete problems.

## Architecture

### Core Components

1. **Generator** (`src/Generator.java`): Main generation engine orchestrating the entire PCG pipeline
2. **EntityGenerator** (`src/EntityGenerator.java`): Handles placement of game entities (boxes, goals, player)
3. **Templates** (`src/Templates.java`): Selection of different 5x5 room templates for layout diversity
4. **Solver Integration** (`src/solver/`): Complete Sokoban solver for puzzle validation
5. **Simulator** (`src/Simulator.java`): Simulates solutions to extract puzzle metrics
6. **GUI** (`src/GUI.java`): Interactive puzzle player interface

### Generation Pipeline

```
Template Selection → Grid Assembly → Entity Placement → Validation → Metric Extraction → Quality Assessment
```

## Running

### Basic Usage

```bash
# Compile the project
javac -d out $(find src -name "*.java")

# Generate a single random puzzle
./run_gen.sh

# Launch interactive GUI
./run_gui.sh

# Bulk generation (250 puzzles)
./run_solutions.sh
```

### GUI Controls

- **Arrow Keys**: Move player
- **Space**: Reset current level
- **Comma (`,`)**: Advance to next level

## Playing Levels

When launching the GUI, you'll be prompted to select a level set:

- **`-1`**: Generate and play random puzzles
- **`0`**: Default pre-generated level set
- **`1`**: Top 10 composite-rating levels (used in Section 6.3.2 of report)
- **`2`**: Levels for testing individual metrics

## 🔧 Configuration

### Generation Parameters

Key parameters can be modified in `Generator.java`:

```java
// Grid size constraints
private static final int MIN_DIV = 3;
private static final int MAX_DIV = 3;  // Currently set equal for 9×9 grids

// Entity constraints  
private static final int MIN_ENTITIES = 6;
private static final int MAX_ENTITIES = 6;  // Currently set equal for 6 boxes

// Generation time limit
private static final int TIMELIMIT = 600000;  // 10 minutes in milliseconds
```

## Metrics and Quality Assessment

The system evaluates generated puzzles using multiple metrics:

### Primary Metrics
- **Moves**: Total player movements required
- **Pushes**: Total box pushes needed
- **Directional Pushes**: Number of continuous push sequences
- **Reverse Pushes**: Pushes away from goal positions
- **Box Changes**: Frequency of switching between different boxes
- **Corner Goals**: Number of goals placed in corner positions

### Composite Rating
Puzzles receive a composite quality score using weighted metrics:

```java
rating = (a×M) + (b×P) + (c×D) + (d×I) + (e×C) - (f×G) - (g×B) + R
```

Where weights are carefully tuned based on puzzle design principles.

## 📁 Project Structure

```
sokoban-pcg/
    src/
        Generator.java          # Main generation engine
        EntityGenerator.java    # Entity placement logic
        Level.java              # Level representation and management
        GUI.java                # Interactive game interface
        Simulator.java          # Solution simulation for metrics
        Templates.java          # Room template definitions
        Point.java              # 2D coordinate utilities
        Tile.java               # Game tile type definitions
        Util.java               # File I/O and format conversion
        solver/                 # Integrated Sokoban solver
            SolverMain.java
            Solver.java
            State.java
            Search.java
            ...
    levels/                    # Generated and preset levels
    results/                   # Generation analysis results
    graphics/                  # Game sprites and assets
    run_gen.sh                 # Single generation script
    run_gui.sh                 # GUI launcher script
    run_solutions.sh           # Batch generation script
    README.md
```

### Experimental Configurations

The current configuration generates 9×9 puzzles with 6 boxes, as used in the dissertation experiments. This can be modified for different research requirements.

## Results and Analysis

Generated puzzles are automatically analysed and results stored in `results/` directory:

- **Composite ratings**: Overall puzzle quality scores
- **Individual metrics**: Detailed breakdown of all measured parameters  
- **Statistical analysis**: Distribution and correlation data
- **Top performers**: Highest-rated puzzles for further study

## Contributing

This is an academic research project. If you're interested in extending or building upon this work:

1. Fork the repository
2. Create a feature branch
3. Make your changes with appropriate documentation
4. Submit a pull request with detailed explanation

## Academic Context

This project explores PCG for PSPACE-complete problems, specifically investigating:

- Computational complexity challenges in automated puzzle generation
- Quality assessment methodologies for procedurally generated content
- Template-based generation systems for constraint-heavy domains
- Integration of solvers in the generation pipeline

## Troubleshooting

### Common Issues

**OutOfMemoryError during generation**: 
- Increase JVM heap size: `java -Xmx2g -cp out Generator`
- Reduce generation time limit in `Generator.java`

**No valid puzzles generated**:
- Verify entity constraints are not too restrictive
    For instance, setting too low of a grid size for the number of entities provided (Exceeding max density)
- Adjust `TIMELIMIT` constant if reaching timeout

## License

This project was developed for academic research purposes. Please cite appropriately if used in academic work.