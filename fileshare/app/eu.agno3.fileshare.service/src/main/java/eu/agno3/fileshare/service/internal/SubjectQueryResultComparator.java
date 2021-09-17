/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Comparator;

import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.model.query.UserQueryResult;
import eu.agno3.fileshare.service.config.TrustLevelConfiguration;


/**
 * @author mbechler
 *
 */
public class SubjectQueryResultComparator implements Comparator<SubjectQueryResult> {

    private TrustLevelConfiguration trustLevelConfiguration;


    /**
     * @param trustLevelConfiguration
     */
    public SubjectQueryResultComparator ( TrustLevelConfiguration trustLevelConfiguration ) {
        this.trustLevelConfiguration = trustLevelConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( SubjectQueryResult o1, SubjectQueryResult o2 ) {

        TrustLevel t1 = this.trustLevelConfiguration.getTrustLevel(o1.getTrustLevel());
        TrustLevel t2 = this.trustLevelConfiguration.getTrustLevel(o2.getTrustLevel());
        int res = 0;

        if ( t1 == null && t2 == null ) {}
        else if ( t1 == null ) {
            return 1;
        }
        else if ( t2 == null ) {
            return -1;
        }
        else {
            res = -1 * Float.compare(t1.getPriority(), t2.getPriority());

            if ( res != 0 ) {
                return res;
            }
        }

        res = o1.getType().compareTo(o2.getType());
        if ( res != 0 ) {
            return res;
        }

        if ( o1 instanceof UserQueryResult && o2 instanceof UserQueryResult ) {
            return ( (UserQueryResult) o1 ).getUserDisplayName().compareTo( ( (UserQueryResult) o2 ).getUserDisplayName());
        }
        else if ( o1 instanceof GroupQueryResult && o2 instanceof GroupQueryResult ) {
            return ( (GroupQueryResult) o1 ).getName().compareTo( ( (GroupQueryResult) o2 ).getName());
        }
        return o1.getId().compareTo(o2.getId());
    }
}
