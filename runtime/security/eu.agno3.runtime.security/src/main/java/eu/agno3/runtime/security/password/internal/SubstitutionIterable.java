/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public class SubstitutionIterable implements Iterable<Map<Character, Character>> {

    private Map<Character, List<Character>> applicableSubs;


    /**
     * @param applicableSubs
     */
    public SubstitutionIterable ( Map<Character, List<Character>> applicableSubs ) {
        this.applicableSubs = applicableSubs;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Map<Character, Character>> iterator () {
        return new SubstitutionIterator(this.applicableSubs);
    }

}
