/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public class KeyboardGraph {

    private final String type;
    private final float averageDegree;
    private final Map<Character, char[][]> adjacencies;


    /**
     * @param type
     * @param adjacencies
     * 
     */
    public KeyboardGraph ( String type, Map<Character, char[][]> adjacencies ) {
        this.type = type;
        this.adjacencies = adjacencies;
        int num = 0;
        int neighbors = 0;
        for ( char[][] charEntry : adjacencies.values() ) {
            num += 1;
            neighbors += charEntry.length;
        }
        this.averageDegree = (float) neighbors / num;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @return the startingPositions
     */
    public int getStartingPositions () {
        return this.adjacencies.size();
    }


    /**
     * @return the averageDegree
     */
    public float getAverageDegree () {
        return this.averageDegree;
    }


    /**
     * @param ch
     * @return a list of adjancencies for the given character ( array of two element array (first is unshifted, second
     *         is shifted)
     */
    public char[][] getAdjacents ( char ch ) {
        char[][] found = this.adjacencies.get(ch);
        if ( found == null ) {
            return new char[][] {};
        }
        return found;
    }

}
