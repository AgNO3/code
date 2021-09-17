/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.agno3.runtime.security.password.internal.generated.AdjacencyGraphs;


/**
 * @author mbechler
 *
 */
public class SpatialMatcher implements PasswordMatcher {

    private List<KeyboardGraph> graphs;


    /**
     * @param graphs
     * 
     */
    public SpatialMatcher ( List<KeyboardGraph> graphs ) {
        this.graphs = graphs;
    }


    /**
     * 
     */
    public SpatialMatcher () {
        this.graphs = new LinkedList<>();
        for ( Entry<String, Map<Character, char[][]>> e : AdjacencyGraphs.ALL_KEYMAPS.entrySet() ) {
            this.graphs.add(new KeyboardGraph(e.getKey(), e.getValue()));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.internal.PasswordMatcher#match(java.lang.String)
     */
    @Override
    public Collection<MatchEntry> match ( String password ) {
        List<MatchEntry> matches = new LinkedList<>();
        for ( KeyboardGraph graph : this.graphs ) {
            addSpatialMatches(matches, graph, password);
        }
        return matches;
    }


    /**
     * @param matches
     * @param graph
     * @param password
     */
    private static void addSpatialMatches ( List<MatchEntry> matches, KeyboardGraph graph, String password ) {
        for ( int pos = 0; pos < password.length() - 1; ) {
            int next = pos + 1;
            int turns = 0;
            int shiftedCount = 0;
            int lastDirection = -1;

            while ( true ) {
                char prevChar = password.charAt(next - 1);
                boolean found = false;
                int foundDirection = -1;
                int curDirection = -1;
                char adjacencents[][] = graph.getAdjacents(prevChar);

                if ( next < password.length() ) {
                    char curChar = password.charAt(next);
                    for ( char[] adjacent : adjacencents ) {
                        curDirection += 1;
                        if ( adjacent == null || ( adjacent[ 0 ] != curChar && adjacent[ 1 ] != curChar ) ) {
                            continue;
                        }

                        found = true;
                        foundDirection = curDirection;

                        if ( adjacent[ 1 ] == curChar ) {
                            shiftedCount++;
                        }

                        if ( lastDirection != foundDirection ) {
                            turns++;
                            lastDirection = foundDirection;
                        }
                        break;
                    }
                }

                if ( found ) {
                    next++;
                    continue;
                }

                if ( next - pos > 2 ) {
                    matches.add(new SpatialMatchEntry(password.substring(pos, next), pos, graph, turns, shiftedCount));
                }

                pos = next;
                break;
            }
        }
    }
}
