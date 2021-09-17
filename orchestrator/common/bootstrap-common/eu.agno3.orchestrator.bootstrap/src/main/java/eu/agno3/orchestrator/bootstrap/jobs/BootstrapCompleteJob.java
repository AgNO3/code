/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.jobs;


import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class BootstrapCompleteJob extends JobImpl {

    private BootstrapContext context;


    /**
     * 
     */
    public BootstrapCompleteJob () {
        super(new ConfigurationJobGroup());
    }


    /**
     * @return the context
     */
    public BootstrapContext getContext () {
        return this.context;
    }


    /**
     * @param context
     *            the context to set
     */
    public void setContext ( BootstrapContext context ) {
        this.context = context;
    }

}
