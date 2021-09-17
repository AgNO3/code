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
public class NamingConflictException extends InvalidEntityNameException {

    /**
     * 
     */
    private static final long serialVersionUID = 3958262960017788763L;
    private String directoryName;


    /**
     * @param conflictingName
     * @param directoryName
     * 
     */
    public NamingConflictException ( String conflictingName, String directoryName ) {
        super(conflictingName);
        this.directoryName = directoryName;
    }


    /**
     * @param conflictingName
     * @param directoryName
     * @param msg
     * @param t
     */
    public NamingConflictException ( String conflictingName, String directoryName, String msg, Throwable t ) {
        super(conflictingName, msg, t);
        this.directoryName = directoryName;
    }


    /**
     * @param conflictingName
     * @param directoryName
     * @param msg
     */
    public NamingConflictException ( String conflictingName, String directoryName, String msg ) {
        super(conflictingName, msg);
        this.directoryName = directoryName;
    }


    /**
     * @param conflictingName
     * @param directoryName
     * @param cause
     */
    public NamingConflictException ( String conflictingName, String directoryName, Throwable cause ) {
        super(conflictingName, cause);
        this.directoryName = directoryName;
    }


    /**
     * @return the directory in which the conflict occured
     */
    public String getDirectoryName () {
        return this.directoryName;
    }

}
