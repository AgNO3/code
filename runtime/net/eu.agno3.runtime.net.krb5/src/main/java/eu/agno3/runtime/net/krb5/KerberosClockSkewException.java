/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 17, 2017 by mbechler
 */
package eu.agno3.runtime.net.krb5;


/**
 * @author mbechler
 *
 */
public class KerberosClockSkewException extends KerberosException {

    /**
     * 
     */
    private static final long serialVersionUID = 1420296112666246079L;


    /**
     * 
     */
    public KerberosClockSkewException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public KerberosClockSkewException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public KerberosClockSkewException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public KerberosClockSkewException ( Throwable t ) {
        super(t);
    }

}
