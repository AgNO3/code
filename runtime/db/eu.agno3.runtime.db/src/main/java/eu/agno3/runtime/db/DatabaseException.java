/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2013 by mbechler
 */
package eu.agno3.runtime.db;


import java.sql.SQLException;

import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 * 
 */
@SafeSerialization
public class DatabaseException extends SQLException {

    /**
     * 
     */
    private static final long serialVersionUID = -7977422761239713918L;


    /**
     * 
     */
    public DatabaseException () {
        super();
    }


    /**
     * @param arg0
     * @param arg1
     */
    public DatabaseException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public DatabaseException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public DatabaseException ( Throwable arg0 ) {
        super(arg0);
    }

}
