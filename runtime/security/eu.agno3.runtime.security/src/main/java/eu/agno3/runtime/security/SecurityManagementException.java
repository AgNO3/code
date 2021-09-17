/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


/**
 * @author mbechler
 *
 */
public class SecurityManagementException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -3748870200212033498L;


    /**
     * 
     */
    public SecurityManagementException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public SecurityManagementException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public SecurityManagementException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public SecurityManagementException ( Throwable t ) {
        super(t);
    }

}
