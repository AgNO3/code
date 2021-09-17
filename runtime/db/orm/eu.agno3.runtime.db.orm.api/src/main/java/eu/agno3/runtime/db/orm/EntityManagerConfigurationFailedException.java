/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2014 by mbechler
 */
package eu.agno3.runtime.db.orm;


/**
 * @author mbechler
 * 
 */
public class EntityManagerConfigurationFailedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1837299878485743341L;


    /**
     * 
     */
    public EntityManagerConfigurationFailedException () {}


    /**
     * @param message
     */
    public EntityManagerConfigurationFailedException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public EntityManagerConfigurationFailedException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public EntityManagerConfigurationFailedException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
