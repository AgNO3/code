/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema;


/**
 * @author mbechler
 * 
 */
public class SchemaException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -8659027731606185726L;


    /**
     * 
     */
    public SchemaException () {}


    /**
     * @param arg0
     */
    public SchemaException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public SchemaException ( Throwable arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public SchemaException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public SchemaException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

}
