/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.10.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap;


/**
 * @author mbechler
 *
 */
public class BootstrapConfigException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6269640333633405790L;


    /**
     * 
     */
    public BootstrapConfigException () {}


    /**
     * @param m
     */
    public BootstrapConfigException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public BootstrapConfigException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public BootstrapConfigException ( String m, Throwable t ) {
        super(m, t);
    }

}
