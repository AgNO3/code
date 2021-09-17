/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info;


/**
 * @author mbechler
 * 
 */
public class SystemInformationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2147285192847861074L;


    /**
     * 
     */
    public SystemInformationException () {}


    /**
     * @param msg
     */
    public SystemInformationException ( String msg ) {
        super(msg);
    }


    /**
     * @param t
     */
    public SystemInformationException ( Throwable t ) {
        super(t);
    }


    /**
     * @param msg
     * @param t
     */
    public SystemInformationException ( String msg, Throwable t ) {
        super(msg, t);
    }

}
