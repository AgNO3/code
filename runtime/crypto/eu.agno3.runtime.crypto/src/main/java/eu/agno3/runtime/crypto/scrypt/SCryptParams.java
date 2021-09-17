/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.scrypt;


/**
 * 
 * @author mbechler
 *
 */
public class SCryptParams {

    private int n;
    private int r;
    private int p;


    /**
     * 
     * @param paramValue
     */
    public SCryptParams ( long paramValue ) {
        this.n = getCpuCostParam(paramValue);
        this.r = getMemoryCostParameter(paramValue);
        this.p = getParallelizationParameter(paramValue);
    }


    /**
     * 
     * @param n
     * @param r
     * @param p
     */
    public SCryptParams ( int n, int r, int p ) {
        this.n = n;
        this.r = r;
        this.p = p;
    }


    /**
     * @return the n
     */
    public int getN () {
        return this.n;
    }


    /**
     * @return the p
     */
    public int getP () {
        return this.p;
    }


    /**
     * @return the r
     */
    public int getR () {
        return this.r;
    }


    private static int getParallelizationParameter ( long parameters ) {
        return (int) parameters & 0xff;
    }


    private static int getCpuCostParam ( long parameters ) {
        return (int) Math.pow(2, parameters >> 16 & 0xffff);
    }


    private static int getMemoryCostParameter ( long parameters ) {
        return (int) parameters >> 8 & 0xff;
    }


    /**
     * @return an encoded parameter value
     */
    public long toLong () {
        return ( log2(this.n) << 16 ) + ( this.r << 8 ) + this.p;
    }


    private static int log2 ( int bits ) {
        if ( bits == 0 ) {
            throw new IllegalArgumentException("log2(0)"); //$NON-NLS-1$
        }
        return 31 - Integer.numberOfLeadingZeros(bits);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.n;
        result = prime * result + this.p;
        result = prime * result + this.r;
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SCryptParams other = (SCryptParams) obj;
        if ( this.n != other.n )
            return false;
        if ( this.p != other.p )
            return false;
        if ( this.r != other.r )
            return false;
        return true;
    }
    // -GENERATED
}