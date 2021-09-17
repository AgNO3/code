/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class SubstitutionIterator implements Iterator<Map<Character, Character>> {

    private Map<Character, List<Character>> applicableSubs;
    private Map<Character, Integer> positions = new HashMap<>();
    private Map<Character, Character> state = new HashMap<>();

    private ArrayList<Character> sourceList;


    /**
     * @param applicableSubs
     */
    public SubstitutionIterator ( Map<Character, List<Character>> applicableSubs ) {
        this.applicableSubs = applicableSubs;
        this.sourceList = new ArrayList<>(applicableSubs.keySet());
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return !this.allButLastAtLastPosition();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public Map<Character, Character> next () {
        int sourceListSize = this.sourceList.size();
        for ( int i = 0; i < sourceListSize; i++ ) {
            char c = this.sourceList.get(i);
            Integer pos = this.positions.get(c);
            int newPos;
            if ( pos == null ) {
                // initialize and return the first permutation
                newPos = 0;
                this.positions.put(c, newPos);
                this.state.put(c, this.applicableSubs.get(c).get(newPos));
                continue;
            }

            newPos = pos + 1;

            int sublength = this.applicableSubs.get(c).size();
            if ( newPos >= sublength ) {
                // overflow
                this.positions.put(c, 0);
                this.state.put(c, this.applicableSubs.get(c).get(0));
            }
            else {
                // increment here
                this.positions.put(c, newPos);
                this.state.put(c, this.applicableSubs.get(c).get(newPos));
                break;
            }
        }

        return Collections.unmodifiableMap(this.state);

    }


    /**
     * @return
     */
    private boolean allButLastAtLastPosition () {
        for ( char c : this.sourceList ) {
            Integer pos = this.positions.get(c);
            if ( pos == null || pos != this.applicableSubs.get(c).size() - 1 ) {
                return false;
            }
        }

        return true;
    }
}
