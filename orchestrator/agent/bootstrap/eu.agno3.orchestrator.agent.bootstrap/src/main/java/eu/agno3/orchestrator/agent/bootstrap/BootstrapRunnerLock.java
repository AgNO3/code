/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap;


/**
 * @author mbechler
 *
 */
public final class BootstrapRunnerLock {

    private static boolean HAVE_RUN = false;


    /**
     * 
     */
    private BootstrapRunnerLock () {}


    /**
     * 
     */
    public static final void reenable () {
        HAVE_RUN = false;
    }


    /**
     * @return whether bootstrap process has run
     */
    public static final boolean hasRun () {
        return HAVE_RUN;
    }


    /**
     * 
     */
    public static void setRun () {
        HAVE_RUN = true;
    }
}
