/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.internal;


/**
 * @author mbechler
 *
 */
public class RefreshHolder {

    private DynamicHibernateBundleInfo bundleInfo;
    private String pu;


    /**
     * @param bundleInfo
     * @param pu
     */
    public RefreshHolder ( DynamicHibernateBundleInfo bundleInfo, String pu ) {
        this.bundleInfo = bundleInfo;
        this.pu = pu;
    }


    /**
     * @return the bundleInfo
     */
    public DynamicHibernateBundleInfo getBundleInfo () {
        return this.bundleInfo;
    }


    /**
     * @return the pu
     */
    public String getPu () {
        return this.pu;
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
        result = prime * result + ( ( this.pu == null ) ? 0 : this.pu.hashCode() );
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
        RefreshHolder other = (RefreshHolder) obj;
        if ( this.pu == null ) {
            if ( other.pu != null )
                return false;
        }
        else if ( !this.pu.equals(other.pu) )
            return false;
        return true;
    }
    // -GENERATED

}
