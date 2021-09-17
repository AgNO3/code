/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.concurrent.atomic.AtomicLong;

import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public class RuntimeQuotaEntry {

    private final EntityKey entityId;
    private final AtomicLong usedSize;


    /**
     * @param id
     * @param childrenSize
     */
    public RuntimeQuotaEntry ( EntityKey id, long childrenSize ) {
        this.entityId = id;
        this.usedSize = new AtomicLong(childrenSize);
    }


    /**
     * @return the entityId
     */
    public EntityKey getEntityId () {
        return this.entityId;
    }


    /**
     * @return the currently used quota
     */
    public long getUsedSize () {
        return Math.max(0, this.usedSize.get());
    }


    /**
     * @param sizeDiff
     * @return the new quota size
     */
    public long updateUsedSize ( long sizeDiff ) {
        return this.usedSize.addAndGet(sizeDiff);
    }


    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.entityId == null ) ? 0 : this.entityId.hashCode() );
        return result;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        RuntimeQuotaEntry other = (RuntimeQuotaEntry) obj;
        if ( this.entityId == null ) {
            if ( other.entityId != null )
                return false;
        }
        else if ( !this.entityId.equals(other.entityId) )
            return false;
        return true;
    }

}
