/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class GroupNameConflictException extends GroupNameInvalidException {

    /**
     * 
     */
    private static final long serialVersionUID = 3308293374629459374L;


    /**
     * 
     */
    public GroupNameConflictException () {}


    /**
     * @param invalidName
     * @param msg
     * @param t
     */
    public GroupNameConflictException ( String invalidName, String msg, Throwable t ) {
        super(invalidName, msg, t);
    }


    /**
     * @param invalidName
     * @param msg
     */
    public GroupNameConflictException ( String invalidName, String msg ) {
        super(invalidName, msg);
    }


    /**
     * @param invalidName
     * @param cause
     */
    public GroupNameConflictException ( String invalidName, Throwable cause ) {
        super(invalidName, cause);
    }

}
