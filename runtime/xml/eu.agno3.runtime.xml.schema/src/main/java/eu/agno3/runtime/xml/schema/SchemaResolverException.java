/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.08.2013 by mbechler
 */
package eu.agno3.runtime.xml.schema;


/**
 * @author mbechler
 * 
 */
public class SchemaResolverException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1811114795937425044L;


    /**
     * 
     */
    public SchemaResolverException () {
        super();
    }


    /**
     * @param arg0
     * @param arg1
     */
    public SchemaResolverException ( String arg0, Throwable arg1 ) {
        super(arg0, arg1);
    }


    /**
     * @param arg0
     */
    public SchemaResolverException ( String arg0 ) {
        super(arg0);
    }


    /**
     * @param arg0
     */
    public SchemaResolverException ( Throwable arg0 ) {
        super(arg0);
    }

}
