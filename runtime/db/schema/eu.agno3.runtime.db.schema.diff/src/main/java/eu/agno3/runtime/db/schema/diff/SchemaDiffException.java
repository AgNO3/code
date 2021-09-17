/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2014 by mbechler
 */
package eu.agno3.runtime.db.schema.diff;


/**
 * @author mbechler
 * 
 */
public class SchemaDiffException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8137990475550201305L;


    /**
     * 
     */
    public SchemaDiffException () {}


    /**
     * @param arg0
     */
    public SchemaDiffException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public SchemaDiffException ( Throwable arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     * @param arg1
     */
    public SchemaDiffException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public SchemaDiffException ( String arg0, Throwable arg1, boolean arg2, boolean arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

}
