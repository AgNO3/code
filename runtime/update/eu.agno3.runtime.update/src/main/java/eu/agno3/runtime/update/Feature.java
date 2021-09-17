/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.09.2015 by mbechler
 */
package eu.agno3.runtime.update;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class Feature implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6132795646283994280L;

    private String id;
    private String version;


    /**
     * 
     */
    public Feature () {}


    /**
     * @param id
     * @param version
     */
    public Feature ( String id, String version ) {
        this.id = id;
        this.version = version;
    }


    /**
     * @return the id
     */
    public String getId () {
        return this.id;
    }


    /**
     * @param id
     *            the id to set
     */
    public void setId ( String id ) {
        this.id = id;
    }


    /**
     * @return the version
     */
    public String getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( String version ) {
        this.version = version;
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
        result = prime * result + ( ( this.id == null ) ? 0 : this.id.hashCode() );
        result = prime * result + ( ( this.version == null ) ? 0 : this.version.hashCode() );
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
        Feature other = (Feature) obj;
        if ( this.id == null ) {
            if ( other.id != null )
                return false;
        }
        else if ( !this.id.equals(other.id) )
            return false;
        if ( this.version == null ) {
            if ( other.version != null )
                return false;
        }
        else if ( !this.version.equals(other.version) )
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
        return this.id + "-" + this.version; //$NON-NLS-1$
    }
}
