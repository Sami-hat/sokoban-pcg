import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.File;

/* Utility class for file I/O and conversion between formats */
public class Util 
{

    /* Function to convert a 2D matrix to Sokoban format */
    public static String convertToSokobanFormat(int[][] grid) 
    {
        StringBuilder sokobanFormat = new StringBuilder();
        for (int[] row : grid) 
        {
            for (int cell : row) 
            {
                switch (cell) 
                {
                    case 0:
                        sokobanFormat.append(' '); /* Floor */
                        break;
                    case 1:
                        sokobanFormat.append('#'); /* Wall */
                        break;
                    case 2:
                        sokobanFormat.append('@'); /* Player */
                        break;
                    case 3:
                        sokobanFormat.append('$'); /* Box */
                        break;
                    case 4:
                        sokobanFormat.append('.'); /* Goal */
                        break;
                    case 5:
                        sokobanFormat.append('*'); /* Box on Goal */
                        break;
                    case 6:
                        sokobanFormat.append('+'); /* Player on Goal */
                        break;
                    default:
                        sokobanFormat.append('#'); /* Unknown - Wall */
                        break;
                }
            }
            sokobanFormat.append('\n');
        }
        return sokobanFormat.toString();
    }

    /* Function to convert a file in Sokoban format to a 2D matrix */
    public static int[][] convertFromSokobanFormat(String filepath) 
    {
        StringBuilder sokobanFormat = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                sokobanFormat.append(line);
                sokobanFormat.append('\n');
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return convertToMatrix(sokobanFormat.toString());
    }

    /* Function to write the Sokoban format string to a file */
    public static String writeToFile(long seed, int[][] grid) 
    {
        String pathname = LEVELS_PATH + seed;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathname))) 
        {
            writer.write(convertToSokobanFormat(grid));
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return pathname;
    }

    /* Function to read the Sokoban format string from a file */
    public static String readFromFile(String filepath) 
    {
        StringBuilder sokobanFormat = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
                sokobanFormat.append(line);
                sokobanFormat.append('\n');
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return sokobanFormat.toString();
    }

    /* Function to convert a Sokoban format string to a 2D matrix */
    public static int[][] convertToMatrix(String sokobanFormat) 
    {
        String[] lines = sokobanFormat.split("\n");
        int rows = lines.length;
        int cols = lines[0].length();
        int[][] grid = new int[rows][cols];

        for (int i = 0; i < rows; i++) 
        {
            for (int j = 0; j < cols; j++) 
            {
                switch (lines[i].charAt(j)) 
                {
                    case ' ':
                        grid[i][j] = 0; /* Floor */
                        break;
                    case '#':
                        grid[i][j] = 1; /* Wall */
                        break;
                    case '@':
                        grid[i][j] = 2; /* Player */
                        break;
                    case '$':
                        grid[i][j] = 3; /* Box */
                        break;
                    case '.':
                        grid[i][j] = 4; /* Goal */
                        break;
                    case '*':
                        grid[i][j] = 4; /* Box on Goal */
                        break;
                    case '+':
                        grid[i][j] = 4; /* Player on Goal */
                        break;
                    default:
                        grid[i][j] = 1; /* Unknown - Wall */
                        break;
                }
            }
        }
        return grid;
    }

    /* Append seed to list of total level seeds */
    public static void appendToFile(long seed, String pathname) 
    {
        try (FileWriter fw = new FileWriter(pathname, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) 
                {
            out.println(seed);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    /* Function to read a specific line from a file */
    public static long readLineFromFile(int lineIndex, String pathname) 
    {
        String line = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathname))) 
        {
            for (int i = 0; i <= lineIndex; i++) 
            {
                line = reader.readLine();
                if (line == null) 
                {
                    break;
                }
            }
            reader.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return line != null ? Long.parseLong(line) : -1;
    }

    /* Function to get the total number of lines in a file */
    public static int getTotalLines(String levelSet) 
    {
        String pathname = Util.getLevelSet(levelSet);
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathname))) 
        {
            while (reader.readLine() != null) 
            {
                lines++;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return lines;
    }

    /* Function to get the path of a specific level set */
    public static String getLevelSet(String levelSet) 
    {
        if (!levelSet.equals("0"))
            return LEVELS_PATH + "levelset_" + levelSet + ".txt";
        return LEVEL_SET_PATH;
    }

    /* Function to get the top rating of a levelset */
    public static void getTopRating() 
    {
        System.out.println("Getting top rating...");
        String fileName = "results/pushes.txt";

        try 
        {
            String content = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);

            /* Regex to match: "TOP RATING #<trial>: <rating>" */
            Pattern pattern = Pattern.compile("TOP RATING #\\d+:\\s*(\\d+(\\.\\d+)?)");
            Matcher matcher = pattern.matcher(content);

            List<Double> topRatings = new ArrayList<>();
            while (matcher.find()) 
            {
                double rating = Double.parseDouble(matcher.group(1));
                topRatings.add(rating);
            }

            System.out.println("Top Ratings: " + topRatings);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    /* Parse metrics from a file */
    public static void parseMetrics() 
    {
        List<Integer> movesList = new ArrayList<>();
        List<Integer> pushesList = new ArrayList<>();
        List<Integer> drPushesList = new ArrayList<>();
        List<Integer> revPushesList = new ArrayList<>();
        List<Integer> boxChangesList = new ArrayList<>();
        List<Integer> cornerGoalsList = new ArrayList<>();

        String filePath = "results/critical_appraisal/parberry2011.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) 
        {
            String line;

            int moves = 0;
            int pushes = 0;
            int dirPushes = 0;
            int revPushes = 0;
            int boxChanges = 0;
            int cornerGoals = 0;

            while ((line = br.readLine()) != null)
            {
                line = line.trim();

                if (line.startsWith("MOVES:")) 
                {
                    String[] parts = line.split("\\|");

                    for (String p : parts) 
                    {
                        p = p.trim();

                        if (p.startsWith("MOVES:")) 
                        {
                            moves = parseIntegerValue(p, "MOVES:");
                        } 
                        else if (p.startsWith("PUSHES:")) 
                        {
                            pushes = parseIntegerValue(p, "PUSHES:");
                        } 
                        else if (p.contains("DIR PUSHES:")) 
                        {
                            dirPushes = parseIntegerValue(p, "DIR PUSHES:");
                        } 
                        else if (p.contains("REV PUSHES:")) 
                        {
                            revPushes = parseIntegerValue(p, "REV PUSHES:");
                        } 
                        else if (p.startsWith("BOX CHANGES:")) 
                        {
                            boxChanges = parseIntegerValue(p, "BOX CHANGES:");
                        } 
                        else if (p.startsWith("CORNER GOALS:")) 
                        {
                            cornerGoals = parseIntegerValue(p, "CORNER GOALS:");
                        }
                    }

                    if (moves == -1) continue;
                    movesList.add(moves);
                    pushesList.add(pushes);
                    drPushesList.add(dirPushes);
                    revPushesList.add(revPushes);
                    boxChangesList.add(boxChanges);
                    cornerGoalsList.add(cornerGoals);;
                }

            }
            movesList.sort(Collections.reverseOrder());
            pushesList.sort(Collections.reverseOrder());

            System.out.println("MOVES: " + movesList);
            System.out.println("PUSHES: " + pushesList);
            System.out.println("DIR PUSHES: " + drPushesList);
            System.out.println("REV PUSHES: " + revPushesList);
            System.out.println("BOX CHANGES: " + boxChangesList);
            System.out.println("CORNER GOALS: " + cornerGoalsList);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    /* Parse an integer after a given prefix */
    private static int parseIntegerValue(String text, String prefix) 
    {
        String numberStr = text.replace(prefix, "").trim();
        return Integer.parseInt(numberStr);
    }

    /* Clear all non-levelset files from directory */
    public static void clearAll() 
    {
        File directory = new File(LEVELS_PATH);
        File[] files = directory.listFiles((dir, name) -> !name.startsWith("levelset"));
        if (files != null) {
            for (File file : files) 
            {
                if (file.isFile()) 
                    file.delete();
            }
        }
    }

    /* Sort the results by rating in descending order */
    public static void sortByRating() throws IOException 
    {
        Path inputFile = Paths.get("results/9x9_6B/pushes_id.txt");
        Path outputFile = Paths.get("results/9x9_6B/pushes_sorted_id.txt");

        List<String> lines = Files.readAllLines(inputFile);

        List<String> sortedLines = lines.stream()
            .sorted((line1, line2) -> 
            {
                double rating1 = extractRating(line1);
                double rating2 = extractRating(line2);
                return Double.compare(rating2, rating1); /* Descending order */
            })
            .collect(Collectors.toList());

        Files.write(outputFile, sortedLines);
    }

    /* Helper method to extract the rating from a line */
    private static double extractRating(String line) {
        String ratingStr = line.split("\\|")[0].replace("RATING:", "").trim();
        return Double.parseDouble(ratingStr);
    }

    public static void main(String[] args) throws IOException 
    {
        /* Uncommented when necessary, file-by-file analysis */
        Util.clearAll();
        // Util.getTopRating();
        // Util.parseMetrics();
        // Util.sortByRating();
    }

    // File Paths
    public static final String LEVEL_SET_PATH = "levels/levelset.txt";
    public static final String LEVELS_PATH = "levels/";
    public static final String GRAPHICS_PATH = "graphics/";

}
