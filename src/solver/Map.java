// Credits to the original authors for providing the game solver code
// https://github.com/Marwes/sokoban/tree/master/src

package solver;

import java.util.ArrayList;
import java.util.Arrays;

public final class Map {


    public Map(ArrayList<String> map)
    {
        startMap = map;
        height = map.size();
        width = 0;
        for (int i = 0; i < height; i++) {
            if (map.get(i).length() > width)
            {
                width = map.get(i).length();
            }
        }
        
        

        mapMatrix = new char[height][width];
        for (char[] row : mapMatrix)
            Arrays.fill(row, ' ');
        for (int i = 0; i < map.size(); i++) {
            for (int j = 0; j < map.get(i).length(); j++) {
                char c = startMap.get(i).charAt(j);
                if (c == '#' | c == '.') {
                    mapMatrix[i][j] = startMap.get(i).charAt(j);
                }
                else if (c == '+' | c == '*') {
                    mapMatrix[i][j] = '.';
                }
                else
                {
                    mapMatrix[i][j] = ' ';
                }
            }
        }
        

        inverseMap = new ArrayList<String>();
        for (int i = 0; i < map.size(); i++) {
            String stringLine = new String();
            for (int j = 0; j < map.get(i).length(); j++) {
                char c = startMap.get(i).charAt(j);
                if (c=='$'){
                    stringLine += ".";
                }
                else if(c=='.' | c=='+'){
                    stringLine += "$";
                }
                else{
                    stringLine += c;
                }
            }
            inverseMap.add(stringLine);
        }
    }
    
    public static Map inverted(Map m){
        return new Map(m.inverseMap);
    }
    
    public int getHeight()
    {
        return this.height;
    }
    public int getWidth()
    {
        return this.width;
    }

    public boolean isEmpty(int x, int y)
    {
        char c = mapMatrix[y][x];
        return (c == ' ' | c == '@' | c == '.' | c =='*');
        
        
    }

    public boolean isWall(int x, int y)
    {
        char c = mapMatrix[y][x];
        return (c == '#');
    }

    public boolean isGoal(int x, int y)
    {
        char c = mapMatrix[y][x];
        return (c == '.');
    }
    
    public boolean isGoalOnBox(int x, int y)
    {
        char c = mapMatrix[y][x];
        return (c == '*'); 
    }

    /**
     *
     * @return String representing the map
     */
    public String toString()
    {
        String out = new String("");
        for (int i = 0; i < startMap.size(); i++) {
            if (i==0) {
                out += startMap.get(i);
            } else {
                out += System.getProperty("line.separator")+startMap.get(i);
            }
        }
        return out;
    }
    
    
    public char[][] mapMatrix;
    private ArrayList<String> startMap;
    public ArrayList<String> inverseMap;
    private int height;
    private int width;
}
