/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing;


import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public class ListeningEventScopeResolver {

    /**
     * @param s
     * @return a set of topics for the listening context
     */
    public Set<String> getListeningTopics ( EventScope s ) {

        Set<String> res = new HashSet<>();

        EventScope cur = s;

        while ( cur != null ) {
            res.add("topic://" + cur.getEventTopic()); //$NON-NLS-1$
            cur = cur.getParent();
        }

        return res;
    }
}
