/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.db;




/**
 * @author mbechler
 * 
 */
public class DatabaseConfigurationException extends DatabaseException {

    /**
     * 
     */
    private static final long serialVersionUID = 4037263854065777913L;


    /**
     * 
     */
    public DatabaseConfigurationException () {
        super();
    }


    /**
     * @param arg0
     * @param arg1
     */
    public DatabaseConfigurationException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public DatabaseConfigurationException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public DatabaseConfigurationException ( Throwable arg0 ) {
        super(arg0);
    }

}
