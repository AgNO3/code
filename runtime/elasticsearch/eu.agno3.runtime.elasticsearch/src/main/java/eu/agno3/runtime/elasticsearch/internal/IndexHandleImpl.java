/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2016 by mbechler
 */
package eu.agno3.runtime.elasticsearch.internal;


import eu.agno3.runtime.elasticsearch.IndexHandle;


/**
 * @author mbechler
 *
 */
public class IndexHandleImpl implements IndexHandle {

    private final String readName;
    private String writeName;
    private String backing;
    private String id;


    /**
     * @param id
     * @param readName
     * @param writeName
     * @param backing
     */
    public IndexHandleImpl ( String id, String readName, String writeName, String backing ) {
        this.id = id;
        this.readName = readName;
        this.writeName = writeName;
        this.backing = backing;
    }


    /**
     * @return the id
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * @return the indexName
     */
    @Override
    public String getReadName () {
        return this.readName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexHandle#getWriteName()
     */
    @Override
    public String getWriteName () {
        return this.writeName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.elasticsearch.IndexHandle#getBacking()
     */
    @Override
    public String getBacking () {
        return this.backing;
    }


    // +GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.backing == null ) ? 0 : this.backing.hashCode() );
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        result = prime * result + ( ( this.readName == null ) ? 0 : this.readName.hashCode() );
        result = prime * result + ( ( this.writeName == null ) ? 0 : this.writeName.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        IndexHandleImpl other = (IndexHandleImpl) obj;
        if ( this.backing == null ) {
            if ( other.backing != null )
                return false;
        }
        else if ( !this.backing.equals(other.backing) )
            return false;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        if ( this.readName == null ) {
            if ( other.readName != null )
                return false;
        }
        else if ( !this.readName.equals(other.readName) )
            return false;
        if ( this.writeName == null ) {
            if ( other.writeName != null )
                return false;
        }
        else if ( !this.writeName.equals(other.writeName) )
            return false;
        return true;
    }
    // -GENERATED


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("id %s read: %s write: %s backing: %s", this.id, this.readName, this.writeName, this.backing); //$NON-NLS-1$
    }
}
