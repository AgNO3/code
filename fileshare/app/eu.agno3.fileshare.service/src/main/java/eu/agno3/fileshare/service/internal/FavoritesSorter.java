/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSEntity;


/**
 * @author mbechler
 *
 */
public class FavoritesSorter implements Comparator<VFSEntity>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -249598086626394358L;

    private Map<EntityKey, DateTime> lastUsedByEntity;


    /**
     * @param lastUsedByEntity
     */
    public FavoritesSorter ( Map<EntityKey, DateTime> lastUsedByEntity ) {
        this.lastUsedByEntity = lastUsedByEntity;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( VFSEntity o1, VFSEntity o2 ) {

        DateTime lastUsed1 = this.lastUsedByEntity.get(o1.getEntityKey());
        DateTime lastUsed2 = this.lastUsedByEntity.get(o2.getEntityKey());

        if ( lastUsed1 == null && lastUsed2 == null ) {
            if ( o1.getLocalName() == null && o2.getLocalName() == null ) {
                return 0;
            }
            else if ( o1.getLocalName() == null ) {
                return 1;
            }
            else if ( o2.getLocalName() == null ) {
                return -1;
            }
            return o1.getLocalName().compareTo(o2.getLocalName());
        }
        else if ( lastUsed1 == null ) {
            return 1;
        }
        else if ( lastUsed2 == null ) {
            return -1;
        }

        return -1 * lastUsed1.compareTo(lastUsed2);
    }
}
