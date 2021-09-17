/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


/**
 * @author mbechler
 * 
 */
public class JobBuilderException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4822293360587463686L;


    /**
     * 
     */
    public JobBuilderException () {}


    /**
     * @param m
     */
    public JobBuilderException ( String m ) {
        super(m);
    }


    /**
     * @param t
     */
    public JobBuilderException ( Throwable t ) {
        super(t);
    }


    /**
     * @param m
     * @param t
     */
    public JobBuilderException ( String m, Throwable t ) {
        super(m, t);
    }

}
