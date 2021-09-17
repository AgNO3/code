/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.runtime.net.krb5;


/**
 * @author mbechler
 *
 */
public class KerberosException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6897704133420143510L;


    /**
     * 
     */
    public KerberosException () {}


    /**
     * @param m
     */
    public KerberosException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public KerberosException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public KerberosException ( String m, Throwable t ) {
        super(m, t);
    }

}
