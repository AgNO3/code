/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2016 by mbechler
 */
package eu.agno3.runtime.security;


import org.apache.shiro.authc.AuthenticationException;


/**
 * @author mbechler
 *
 */
public class UserLicenseLimitExceededRuntimeException extends AuthenticationException {

    /**
     * 
     */
    private static final long serialVersionUID = -5210077189236641965L;

    private long count;
    private long limit;


    /**
     * @param limit
     * @param uc
     * 
     */
    UserLicenseLimitExceededRuntimeException ( UserLicenseLimitExceededException e ) {
        super();
        this.count = e.getCount();
        this.limit = e.getLimit();
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

}
