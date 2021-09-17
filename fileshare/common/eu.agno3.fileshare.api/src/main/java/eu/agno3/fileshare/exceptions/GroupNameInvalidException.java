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
public class GroupNameInvalidException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -411856543297368621L;
    private String invalidName;


    /**
     * 
     */
    public GroupNameInvalidException () {}


    /**
     * @param invalidName
     * @param msg
     * @param t
     */
    public GroupNameInvalidException ( String invalidName, String msg, Throwable t ) {
        super(msg, t);
        this.invalidName = invalidName;
    }


    /**
     * @param invalidName
     * @param msg
     */
    public GroupNameInvalidException ( String invalidName, String msg ) {
        super(msg);
        this.invalidName = invalidName;
    }


    /**
     * @param invalidName
     * @param cause
     */
    public GroupNameInvalidException ( String invalidName, Throwable cause ) {
        super(cause);
        this.invalidName = invalidName;
    }


    /**
     * @return the invalidName
     */
    public String getInvalidName () {
        return this.invalidName;
    }
}
