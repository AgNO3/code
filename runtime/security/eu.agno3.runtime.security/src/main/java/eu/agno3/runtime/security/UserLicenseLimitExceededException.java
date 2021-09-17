/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2016 by mbechler
 */
package eu.agno3.runtime.security;


/**
 * @author mbechler
 *
 */
public class UserLicenseLimitExceededException extends SecurityManagementException {

    /**
     * 
     */
    private static final long serialVersionUID = -5210077189236641965L;

    private long count;
    private long limit;


    /**
     * 
     */
    public UserLicenseLimitExceededException () {}


    /**
     * @param m
     * @param t
     */
    public UserLicenseLimitExceededException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public UserLicenseLimitExceededException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public UserLicenseLimitExceededException ( Throwable t ) {
        super(t);
    }


    /**
     * @param limit
     * @param uc
     * 
     */
    public UserLicenseLimitExceededException ( long uc, long limit ) {
        super();
        this.count = uc;
        this.limit = limit;
    }


    /**
     * @return the limit
     */
    public long getLimit () {
        return this.limit;
    }


    /**
     * @return the count
     */
    public long getCount () {
        return this.count;
    }


    /**
     * @return a wrapped runtime exception
     */
    public UserLicenseLimitExceededRuntimeException asRuntimeException () {
        return new UserLicenseLimitExceededRuntimeException(this);
    }
}
