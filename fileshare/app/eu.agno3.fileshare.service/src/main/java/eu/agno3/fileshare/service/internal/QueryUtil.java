/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;


/**
 * @author mbechler
 *
 */
public final class QueryUtil {

    /**
     * 
     */
    private QueryUtil () {}

    /**
     * 
     */
    private static final String QUERY_WILDCARDS = "%_"; //$NON-NLS-1$


    /**
     * @param set
     * @return the subject ids
     */
    public static List<UUID> toUUIDs ( Set<Group> set ) {
        List<UUID> ids = new ArrayList<>();
        for ( Subject g : set ) {
            ids.add(g.getId());
        }
        return ids;
    }


    /**
     * @param query
     * @return the escaped approximate query (LIKE)
     */
    public static String escapeQuery ( String query ) {
        return String.format("%s%%", StringUtils.replaceChars(query, QUERY_WILDCARDS, StringUtils.EMPTY).toLowerCase()); //$NON-NLS-1$
    }

}
