/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


/**
 * @author mbechler
 *
 */
public class InvalidEntityNameException extends InvalidEntityException {

    /**
     * 
     */
    private static final long serialVersionUID = 6743164838701969451L;
    private String invalidName;


    /**
     * @param invalidName
     * 
     */
    public InvalidEntityNameException ( String invalidName ) {
        this.invalidName = invalidName;
    }


    /**
     * @param invalidName
     * @param msg
     * @param t
     */
    public InvalidEntityNameException ( String invalidName, String msg, Throwable t ) {
        super(msg, t);
        this.invalidName = invalidName;
    }


    /**
     * @param invalidName
     * @param msg
     */
    public InvalidEntityNameException ( String invalidName, String msg ) {
        super(msg);
        this.invalidName = invalidName;
    }


    /**
     * @param invalidName
     * @param cause
     */
    public InvalidEntityNameException ( String invalidName, Throwable cause ) {
        super(cause);
        this.invalidName = invalidName;
    }


    /**
     * @return the invalid name
     */
    public String getInvalidName () {
        return this.invalidName;
    }

}
