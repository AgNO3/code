/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


/**
 * @author mbechler
 * 
 */
public class MatcherException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5291310982068155350L;


    /**
     * 
     */
    public MatcherException () {
        super();
    }


    /**
     * @param m
     * @param t
     */
    public MatcherException ( String m, Throwable t ) {
        super(m, t);
    }


    /**
     * @param m
     */
    public MatcherException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public MatcherException ( Throwable t ) {
        super(t);
    }

}
