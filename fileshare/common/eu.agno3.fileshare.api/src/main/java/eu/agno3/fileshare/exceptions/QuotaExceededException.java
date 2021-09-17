/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class QuotaExceededException extends EntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 7896220889888778677L;
    private long quota;
    private long exceedBy;


    /**
     * 
     */
    public QuotaExceededException () {
        super();
    }


    /**
     * @param quota
     * @param exceedBy
     */
    public QuotaExceededException ( long quota, long exceedBy ) {
        super();
        this.quota = quota;
        this.exceedBy = exceedBy;
    }


    /**
     * @param quota
     * @param exceedBy
     * @param msg
     * @param t
     */
    public QuotaExceededException ( long quota, long exceedBy, String msg, Throwable t ) {
        super(msg, t);
        this.quota = quota;
        this.exceedBy = exceedBy;
    }


    /**
     * @param quota
     * @param exceedBy
     * @param msg
     */
    public QuotaExceededException ( long quota, long exceedBy, String msg ) {
        super(msg);
        this.quota = quota;
        this.exceedBy = exceedBy;
    }


    /**
     * @param quota
     * @param exceedBy
     * @param cause
     */
    public QuotaExceededException ( long quota, long exceedBy, Throwable cause ) {
        super(cause);
        this.quota = quota;
        this.exceedBy = exceedBy;
    }


    /**
     * @return the quota
     */
    public long getQuota () {
        return this.quota;
    }


    /**
     * @return the exceedBy
     */
    public long getExceedBy () {
        return this.exceedBy;
    }
}
