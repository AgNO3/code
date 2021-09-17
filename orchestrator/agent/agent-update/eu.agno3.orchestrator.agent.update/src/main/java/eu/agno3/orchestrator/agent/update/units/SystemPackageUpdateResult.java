/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import java.util.Set;

import eu.agno3.orchestrator.system.base.execution.Status;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class SystemPackageUpdateResult extends StatusOnlyResult {

    /**
     * 
     */
    private static final long serialVersionUID = -4573366955879295193L;
    private boolean rebootSuggested;
    private Set<String> doAlternativeRestart;


    /**
     * @param s
     * @param rebootSuggested
     * @param doAlternativeRestart
     */
    public SystemPackageUpdateResult ( Status s, boolean rebootSuggested, Set<String> doAlternativeRestart ) {
        super(s);
        this.rebootSuggested = rebootSuggested;
        this.doAlternativeRestart = doAlternativeRestart;
    }


    /**
     * @return the rebootSuggested
     */
    public boolean isRebootSuggested () {
        return this.rebootSuggested;
    }


    /**
     * @param service
     * @return whether a service should be restarted afterwards
     */
    public boolean isRequireRestart ( String service ) {
        return this.doAlternativeRestart.contains(service);
    }

}
