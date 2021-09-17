/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;

import eu.agno3.fileshare.model.EntityKey;

class RuntimeModificationEntry {

    private EntityKey key;
    private Long lastModification;


    /**
     * @param k
     * @param lastMod
     * 
     */
    public RuntimeModificationEntry ( EntityKey k, Long lastMod ) {
        this.key = k;
        this.lastModification = lastMod;
    }


    /**
     * @param lastMod
     * @return the updated value
     */
    public synchronized boolean updateLastModification ( Long lastMod ) {
        if ( this.lastModification == null || ( lastMod != null && lastMod > this.lastModification ) ) {
            this.lastModification = lastMod;
            return true;
        }

        return false;
    }


    /**
     * @return the key
     */
    public EntityKey getEntityId () {
        return this.key;
    }


    /**
     * @return the lastModification
     */
    public Long getLastModification () {
        return this.lastModification;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.key == null ) ? 0 : this.key.hashCode() );
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
        RuntimeModificationEntry other = (RuntimeModificationEntry) obj;
        if ( this.key == null ) {
            if ( other.key != null )
                return false;
        }
        else if ( !this.key.equals(other.key) )
            return false;
        return true;
    }
    // -GENERATED
}