/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2013 by mbechler
 */
package eu.agno3.runtime.ldap.filter;


/**
 * @author mbechler
 * 
 */
public class FilterException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -9086827864136097037L;


    /**
     * 
     */
    public FilterException () {
        super();
    }


    /**
     * @param msg
     * @param t
     */
    public FilterException ( String msg, Throwable t ) {
        super(msg, t);
    }


    /**
     * @param msg
     */
    public FilterException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public FilterException ( Throwable t ) {
        super(t);
    }

}
