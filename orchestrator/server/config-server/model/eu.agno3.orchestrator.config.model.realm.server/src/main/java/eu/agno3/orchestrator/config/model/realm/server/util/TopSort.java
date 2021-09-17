/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 20, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class TopSort {

    private static final Logger log = Logger.getLogger(TopSort.class);


    /**
     * 
     * @param elements
     * @param depends
     * @param compare
     * @return sorted elements
     * @throws TopSortException
     */
    public static <N> List<N> topsort ( Collection<N> elements, Function<N, Collection<N>> depends, Comparator<? super N> compare )
            throws TopSortException {
        if ( elements.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }
        Map<N, Collection<N>> pred = new IdentityHashMap<>(elements.size());
        Map<N, Collection<N>> succ = new IdentityHashMap<>(elements.size());
        Queue<N> usableNodes = compare != null ? new PriorityQueue<>(compare) : new LinkedList<>();

        // build graph
        for ( N elem : elements ) {
            Collection<N> preds = depends.apply(elem);
            if ( preds == null || preds.isEmpty() ) {
                usableNodes.add(elem);
                continue;
            }
            pred.put(elem, preds);
            for ( N p : preds ) {
                if ( !elements.contains(p) ) {
                    throw new TopSortException("Invalid predecessor"); //$NON-NLS-1$
                }
                Collection<N> nsuccs = succ.get(p);
                if ( nsuccs == null ) {
                    nsuccs = new LinkedList<>();
                    succ.put(p, nsuccs);
                }
                nsuccs.add(elem);
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug(pred.keySet());
        }

        // now sort
        List<N> res = new LinkedList<>();
        if ( usableNodes.isEmpty() ) {
            throw new TopSortException("Graph is cyclic"); //$NON-NLS-1$
        }

        while ( !usableNodes.isEmpty() ) {
            N n = usableNodes.poll();
            res.add(n);

            Collection<N> successors = succ.remove(n);
            if ( successors == null ) {
                continue;
            }

            for ( N sn : successors ) {
                Collection<N> preds = pred.get(sn);
                preds.remove(n);
                if ( preds.isEmpty() ) {
                    if ( !usableNodes.contains(sn) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Available " + sn); //$NON-NLS-1$
                        }
                        usableNodes.add(sn);
                    }
                }
            }
            successors.clear();
        }
        if ( res.size() != elements.size() ) {
            throw new TopSortException("Graph is cyclic"); //$NON-NLS-1$
        }
        return res;
    }

    /**
     * @author mbechler
     *
     */
    public static class TopSortException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -8434647149003593264L;


        /**
         * 
         */
        TopSortException () {}


        /**
         * 
         * @param msg
         */
        TopSortException ( String msg ) {
            super(msg);
        }
    }
}