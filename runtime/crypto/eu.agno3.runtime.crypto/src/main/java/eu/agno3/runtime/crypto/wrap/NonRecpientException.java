/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 14, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.wrap;


import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public class NonRecpientException extends CryptoException {

    /**
     * 
     */
    private static final long serialVersionUID = 3622194510766753097L;


    /**
     * 
     */
    public NonRecpientException () {}


    /**
     * @param m
     */
    public NonRecpientException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public NonRecpientException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public NonRecpientException ( String m, Throwable t ) {
        super(m, t);
    }

}
