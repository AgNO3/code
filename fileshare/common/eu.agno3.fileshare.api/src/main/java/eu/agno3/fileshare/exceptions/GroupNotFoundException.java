/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class GroupNotFoundException extends SubjectNotFoundException {

    /**
     * 
     */
    private static final long serialVersionUID = -8187556189093585651L;


    /**
     * 
     */
    public GroupNotFoundException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public GroupNotFoundException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public GroupNotFoundException ( String msg ) {
        super(msg);
    }


    /**
     * @param cause
     */
    public GroupNotFoundException ( Throwable cause ) {
        super(cause);
    }

}
