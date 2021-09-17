/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.dns;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;


/**
 * @author mbechler
 *
 */
public class SRVEntries {

    private List<SRVEntry> origEntries = new ArrayList<>();
    private MultiValuedMap<Integer, SRVEntry> byPriority = new ArrayListValuedHashMap<>();
    private int minTTL = -1;

    private static final Random RANDOM = new Random();


    /**
     * @return the minimum TTL of all entries
     */
    public int getMinTTL () {
        return this.minTTL;
    }


    /**
     * 
     * @param e
     */
    public void add ( SRVEntry e ) {
        this.origEntries.add(e);
        this.byPriority.put(e.getPriority(), e);
        this.minTTL = this.minTTL < 0 ? e.getTTL() : Math.min(this.minTTL, e.getTTL());
    }


    /**
     * 
     */
    public void clear () {
        this.origEntries.clear();
        this.byPriority.clear();
    }


    /**
     * @return the origEntries
     */
    public List<SRVEntry> getEntries () {
        return this.origEntries;
    }


    /**
     * 
     * @param num
     *            limit the number of returned entries or -1 to return entries for the first available priority
     * @return a ordered selection of entries based on RFC2782
     */
    public List<SRVEntry> getEntrySelection ( int num ) {
        Iterator<Integer> prioIterator = getPriorities().iterator();
        List<SRVEntry> found = new LinkedList<>();

        while ( prioIterator.hasNext() && ( ( num < 0 ) || found.size() < num ) ) {
            int prio = prioIterator.next();
            List<SRVEntry> records = new ArrayList<>(this.getByPriority(prio));
            int totalWeight = 0;

            List<SRVEntry> nullWeight = new LinkedList<>();
            for ( SRVEntry e : records ) {
                if ( e.getWeight() > 0 ) {
                    totalWeight += e.getWeight();
                }
                else {
                    nullWeight.add(e);
                }
            }

            // place 0 weights at the beginning, as per RC2782
            records.removeAll(nullWeight);
            records.addAll(0, nullWeight);

            while ( !records.isEmpty() && ( num < 0 || found.size() < num ) ) {
                SRVEntry selected = null;
                int random = RANDOM.nextInt(totalWeight);
                int runningWeight = 0;
                for ( SRVEntry record : records ) {
                    runningWeight += record.getWeight();

                    if ( runningWeight >= random ) {
                        selected = record;
                        break;
                    }
                }

                if ( selected == null ) {
                    throw new IllegalStateException();
                }

                totalWeight -= selected.getWeight();
                records.remove(selected);
                found.add(selected);
            }

            if ( num <= 0 ) {
                break;
            }
        }
        return found;
    }


    /**
     * 
     * @return the defined priorities sorted
     */
    public List<Integer> getPriorities () {
        List<Integer> prios = new ArrayList<>(this.byPriority.keySet());
        Collections.sort(prios);
        return prios;
    }


    /**
     * @param prio
     * @return the SRV records having this priority
     */
    public Collection<SRVEntry> getByPriority ( int prio ) {
        Collection<SRVEntry> collection = this.byPriority.get(prio);
        if ( collection == null ) {
            return Collections.EMPTY_LIST;
        }
        return collection;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.origEntries == null ) ? 0 : this.origEntries.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SRVEntries other = (SRVEntries) obj;
        if ( this.origEntries == null ) {
            if ( other.origEntries != null )
                return false;
        }
        else if ( !this.origEntries.equals(other.origEntries) )
            return false;
        return true;
    }

    // -GENERATED

}
