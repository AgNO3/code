/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.io.Serializable;
import java.util.UUID;


class CacheKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6122305955225439367L;
    private Class<?> type;
    private UUID anchor;


    /**
     * @param type
     * @param anchor
     * 
     */
    public CacheKey ( Class<?> type, UUID anchor ) {
        this.type = type;
        this.anchor = anchor;
    }


    /**
     * @return the anchor
     */
    public UUID getAnchor () {
        return this.anchor;
    }


    /**
     * @return the type
     */
    public Class<?> getType () {
        return this.type;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.anchor == null ) ? 0 : this.anchor.hashCode() );
        result = prime * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        CacheKey other = (CacheKey) obj;
        if ( this.anchor == null ) {
            if ( other.anchor != null )
                return false;
        }
        else if ( !this.anchor.equals(other.anchor) )
            return false;
        if ( this.type == null ) {
            if ( other.type != null )
                return false;
        }
        else if ( !this.type.equals(other.type) )
            return false;
        return true;
    }
    // -GENERATED
}