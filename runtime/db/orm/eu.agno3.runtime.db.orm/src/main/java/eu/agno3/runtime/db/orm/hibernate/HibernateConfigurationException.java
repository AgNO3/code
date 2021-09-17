/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2014 by mbechler
 */
package eu.agno3.runtime.db.orm.hibernate;


/**
 * @author mbechler
 * 
 */
public class HibernateConfigurationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1452907237021521980L;


    /**
     * @param message
     */
    public HibernateConfigurationException ( String message ) {
        super(message);
    }


    /**
     * @param cause
     */
    public HibernateConfigurationException ( Throwable cause ) {
        super(cause);
    }


    /**
     * @param message
     * @param cause
     */
    public HibernateConfigurationException ( String message, Throwable cause ) {
        super(message, cause);
    }

}
